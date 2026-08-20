package com.systemmonitor.vault.workers

import android.content.Context
import android.util.Log
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

    companion object {
        private const val TAG = "VaultCleanupWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            cleanupManager.performCleanup()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup failed (runAttemptCount=$runAttemptCount)", e)
            // Retry a few times in case this was a transient issue (e.g. a file
            // briefly locked by another process), then give up so WorkManager
            // doesn't retry forever.
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}