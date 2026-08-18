package com.systemmonitor.vault.importexport

import android.content.Context
import android.net.Uri
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.repository.VaultAuditRepository
import com.systemmonitor.vault.storage.VaultStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importValidator: ImportValidator,
    private val filePickerManager: FilePickerManager,
    private val fileHashManager: FileHashManager,
    private val duplicateFileDetector: DuplicateFileDetector,
    private val storageManager: VaultStorageManager,
    private val auditRepository: VaultAuditRepository
) {
    private val _importProgress = MutableStateFlow(ImportProgress())
    val importProgress: StateFlow<ImportProgress> = _importProgress.asStateFlow()

    suspend fun importSingleFile(
        uri: Uri,
        parentId: String?,
        allowDuplicates: Boolean = false
    ): Result<VaultFileEntity> {
        _importProgress.value = ImportProgress(status = ImportStatus.VALIDATING)

        // 1. Validation
        val validation = importValidator.validateUri(uri)
        if (validation is ImportValidator.ValidationResult.Invalid) {
            _importProgress.value = ImportProgress(status = ImportStatus.FAILED, errorMessage = validation.reason)
            return Result.failure(Exception(validation.reason))
        }

        val metadata = filePickerManager.resolveUriMetadata(uri)

        // 2. Hash calculation & Duplicate detection
        _importProgress.value = ImportProgress(
            status = ImportStatus.CHECKING_DUPLICATES,
            currentFileName = metadata.fileName
        )
        val fileHash = try {
            context.contentResolver.openInputStream(uri)?.use {
                fileHashManager.calculateSha256(it)
            }
        } catch (e: Exception) {
            null
        }

        if (!allowDuplicates && fileHash != null) {
            val duplicate = duplicateFileDetector.findDuplicateByHash(fileHash)
            if (duplicate != null) {
                val errMsg = "Duplicate file already exists in vault: ${duplicate.name}"
                _importProgress.value = ImportProgress(status = ImportStatus.FAILED, errorMessage = errMsg)
                return Result.failure(Exception(errMsg))
            }
        }

        // 3. Encrypt & Store
        _importProgress.value = ImportProgress(
            status = ImportStatus.ENCRYPTING,
            currentFileName = metadata.fileName
        )

        val result = storageManager.importFile(
            uri = uri,
            fileName = metadata.fileName,
            mimeType = metadata.mimeType,
            parentId = parentId,
            fileHash = fileHash
        )

        return if (result.isSuccess) {
            val entity = result.getOrThrow()
            _importProgress.value = ImportProgress(status = ImportStatus.COMPLETED)
            auditRepository.logEvent("IMPORT", "Imported and encrypted file '${metadata.fileName}' (Hash: ${fileHash?.take(8)}...)")
            Result.success(entity)
        } else {
            val err = result.exceptionOrNull()?.message ?: "Import failed"
            _importProgress.value = ImportProgress(status = ImportStatus.FAILED, errorMessage = err)
            auditRepository.logEvent("IMPORT_FAILED", "Failed to import file '${metadata.fileName}': $err")
            Result.failure(result.exceptionOrNull() ?: Exception(err))
        }
    }

    suspend fun importMultipleFiles(
        uris: List<Uri>,
        parentId: String?,
        allowDuplicates: Boolean = false
    ): List<Result<VaultFileEntity>> {
        val total = uris.size
        if (total == 0) return emptyList()

        val results = mutableListOf<Result<VaultFileEntity>>()
        uris.forEachIndexed { index, uri ->
            _importProgress.value = ImportProgress(
                status = ImportStatus.ENCRYPTING,
                currentFileIndex = index + 1,
                totalFiles = total
            )
            val res = importSingleFile(uri, parentId, allowDuplicates)
            results.add(res)
        }
        _importProgress.value = ImportProgress(
            status = ImportStatus.COMPLETED,
            currentFileIndex = total,
            totalFiles = total
        )
        auditRepository.logEvent("BATCH_IMPORT", "Batch imported ${results.count { it.isSuccess }}/$total files into vault")
        return results
    }
}
