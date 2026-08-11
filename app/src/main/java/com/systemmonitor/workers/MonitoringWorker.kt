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
import com.systemmonitor.repository.LaptopRepository
import com.systemmonitor.domain.model.LaptopStatus
import com.systemmonitor.domain.model.ConnectionMode
import com.systemmonitor.data.network.NetworkResult
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
    private val networkRepository: NetworkRepository,
    private val laptopRepository: LaptopRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        // Background check of all paired laptops status & failover analysis
        try {
            val laptops = laptopRepository.getAllLaptopsList()
            for (laptop in laptops) {
                var isOnline = false
                var activeMode = laptop.connectionMode

                val primaryResult = laptopRepository.checkStatusForLaptop(laptop)
                if (primaryResult is NetworkResult.Success && primaryResult.data) {
                    isOnline = true
                } else {
                    val alternateMode = if (laptop.connectionMode == ConnectionMode.LOCAL) {
                        ConnectionMode.REMOTE
                    } else {
                        ConnectionMode.LOCAL
                    }
                    val altLaptop = laptop.copy(connectionMode = alternateMode)
                    val secondaryResult = laptopRepository.checkStatusForLaptop(altLaptop)
                    if (secondaryResult is NetworkResult.Success && secondaryResult.data) {
                        isOnline = true
                        activeMode = alternateMode
                    }
                }

                val newStatus = if (isOnline) LaptopStatus.ONLINE else LaptopStatus.OFFLINE
                laptopRepository.updateLaptopStatusAndMode(laptop.id, newStatus, activeMode)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Background laptop status analysis failed", e)
        }

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
