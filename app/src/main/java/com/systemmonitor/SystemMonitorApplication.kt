package com.systemmonitor

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.systemmonitor.core.Constants
import com.systemmonitor.workers.FirebaseSyncWorker
import com.systemmonitor.workers.MonitoringWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SystemMonitorApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleBackgroundWork()
    }

    private fun scheduleBackgroundWork() {
        val workManager = WorkManager.getInstance(this)

        val monitoringRequest = PeriodicWorkRequestBuilder<MonitoringWorker>(
            Constants.BATTERY_POLL_INTERVAL_MINUTES, TimeUnit.MINUTES
        ).build()
        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_BATTERY_MONITOR,
            ExistingPeriodicWorkPolicy.KEEP,
            monitoringRequest
        )

        val syncRequest = PeriodicWorkRequestBuilder<FirebaseSyncWorker>(
            Constants.SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
        ).build()
        workManager.enqueueUniquePeriodicWork(
            Constants.WORK_FIREBASE_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
