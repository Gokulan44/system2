package com.systemmonitor.vault.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmonitor.vault.backup.EncryptedBackupManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class VaultBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backupManager: EncryptedBackupManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = backupManager.createBackupArchive()
        return if (result.isSuccess) Result.success() else Result.failure()
    }
}
