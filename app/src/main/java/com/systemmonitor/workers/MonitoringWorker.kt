package com.systemmonitor.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmonitor.core.AppLogger
import com.systemmonitor.repository.BatteryRepository
import com.systemmonitor.repository.MemoryRepository
import com.systemmonitor.repository.NetworkRepository
import com.systemmonitor.repository.StorageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Periodic capture of device telemetry. Each repository capture runs
 * independently and in parallel so one failing metric (e.g. Wi-Fi read
 * throwing SecurityException on a locked-down OEM build) doesn't block
 * the others. As CpuMonitor/AppUsageMonitor/etc. land, add them the same way.
 */
@HiltWorker
class MonitoringWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val batteryRepository: BatteryRepository,
    private val memoryRepository: MemoryRepository,
    private val storageRepository: StorageRepository,
    private val networkRepository: NetworkRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val results = listOf(
            async { runCatching { batteryRepository.captureAndStore() } },
            async { runCatching { memoryRepository.captureAndStore() } },
            async { runCatching { storageRepository.captureAndStore() } },
            async { runCatching { networkRepository.captureAndStore() } }
        ).map { it.await() }

        results.forEachIndexed { index, result ->
            result.onFailure { AppLogger.e(TAG, "Capture #$index failed", it) }
        }

        if (results.all { it.isSuccess }) Result.success() else Result.retry()
    }

    companion object {
        private const val TAG = "MonitoringWorker"
    }
}
