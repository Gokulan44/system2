package com.systemmonitor.vault.importexport

import android.net.Uri
import com.systemmonitor.vault.repository.VaultAuditRepository
import com.systemmonitor.vault.storage.VaultStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultExportManager @Inject constructor(
    private val exportValidator: ExportValidator,
    private val storageManager: VaultStorageManager,
    private val auditRepository: VaultAuditRepository
) {
    private val _exportProgress = MutableStateFlow(ExportProgress())
    val exportProgress: StateFlow<ExportProgress> = _exportProgress.asStateFlow()

    suspend fun exportSingleFile(
        fileId: String,
        outputUri: Uri
    ): Result<Boolean> {
        _exportProgress.value = ExportProgress(status = ExportStatus.VALIDATING)

        val validation = exportValidator.validateExport(fileId, outputUri)
        if (validation is ExportValidator.ExportValidationResult.Invalid) {
            _exportProgress.value = ExportProgress(status = ExportStatus.FAILED, errorMessage = validation.reason)
            return Result.failure(Exception(validation.reason))
        }

        val entity = (validation as ExportValidator.ExportValidationResult.Valid).fileEntity
        _exportProgress.value = ExportProgress(
            status = ExportStatus.DECRYPTING,
            currentFileName = entity.name
        )

        val result = storageManager.exportFile(fileId, outputUri)
        return if (result.isSuccess) {
            _exportProgress.value = ExportProgress(status = ExportStatus.COMPLETED)
            auditRepository.logEvent("EXPORT", "Exported and decrypted file '${entity.name}'")
            Result.success(true)
        } else {
            val err = result.exceptionOrNull()?.message ?: "Export failed"
            _exportProgress.value = ExportProgress(status = ExportStatus.FAILED, errorMessage = err)
            auditRepository.logEvent("EXPORT_FAILED", "Failed to export file '${entity.name}': $err")
            Result.failure(result.exceptionOrNull() ?: Exception(err))
        }
    }
}
