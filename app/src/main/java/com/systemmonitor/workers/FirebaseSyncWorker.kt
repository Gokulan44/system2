package com.systemmonitor.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmonitor.core.AppLogger
import com.systemmonitor.core.DeviceIdManager
import com.systemmonitor.firebase.firestore.BatterySync
import com.systemmonitor.repository.BatteryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class FirebaseSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val batteryRepository: BatteryRepository,
    private val batterySync: BatterySync,
    private val deviceIdManager: DeviceIdManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val since = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
            val summary = batteryRepository.getSummarySince(since)
            batterySync.pushSummary(deviceIdManager.getDeviceId(), summary)
            AppLogger.d(TAG, "Synced battery summary: $summary")
            Result.success()
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Battery sync failed", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "FirebaseSyncWorker"
    }
}
