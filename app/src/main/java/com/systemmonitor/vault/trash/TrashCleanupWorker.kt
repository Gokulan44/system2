package com.systemmonitor.vault.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.trash.SecureDeleteManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class TrashCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val fileDao: VaultFileDao,
    private val secureDeleteManager: SecureDeleteManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val trashFiles = fileDao.getTrashFilesList()

            for (file in trashFiles) {
                val trashedTime = file.trashedAt ?: 0L
                if (trashedTime > 0 && trashedTime < thirtyDaysAgo) {
                    secureDeleteManager.shredFile(File(file.localPath))
                    fileDao.deleteFileById(file.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
