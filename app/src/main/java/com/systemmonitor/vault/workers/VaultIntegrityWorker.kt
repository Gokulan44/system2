package com.systemmonitor.vault.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.repository.VaultAuditRepository
import com.systemmonitor.vault.security.FileIntegrityManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class VaultIntegrityWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val fileDao: VaultFileDao,
    private val integrityManager: FileIntegrityManager,
    private val auditRepository: VaultAuditRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val files = fileDao.getAllFilesList()
            var corruptedCount = 0

            for (file in files) {
                val result = integrityManager.verifyIntegrity(file.id)
                if (result is FileIntegrityManager.IntegrityResult.Corrupted) {
                    corruptedCount++
                    auditRepository.logEvent(
                        "INTEGRITY_CHECK_FAILED",
                        "File '${file.name}' failed integrity check: ${result.reason}"
                    )
                }
            }

            auditRepository.logEvent("INTEGRITY_CHECK_COMPLETED", "Scanned ${files.size} files, found $corruptedCount issues.")
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
