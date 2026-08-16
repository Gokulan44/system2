package com.systemmonitor.vault.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmonitor.vault.storage.StorageCleanupManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class VaultCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cleanupManager: StorageCleanupManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            cleanupManager.performCleanup()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
