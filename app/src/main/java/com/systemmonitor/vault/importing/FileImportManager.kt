package com.systemmonitor.vault.importing

import android.content.Context
import android.net.Uri
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.importexport.DuplicateFileDetector
import com.systemmonitor.vault.importexport.FileHashManager
import com.systemmonitor.vault.importexport.ImportProgress
import com.systemmonitor.vault.importexport.ImportStatus
import com.systemmonitor.vault.storage.VaultStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metadataReader: FileMetadataReader,
    private val validator: FileImportValidator,
    private val duplicateDetector: DuplicateFileDetector,
    private val fileHashManager: FileHashManager,
    private val storageManager: VaultStorageManager
) {
    private val _importProgress = MutableStateFlow(ImportProgress())
    val importProgress: StateFlow<ImportProgress> = _importProgress.asStateFlow()

    /**
     * Standard import path resolving metadata on-the-fly.
     */
    suspend fun importFile(
        uri: Uri,
        parentId: String? = null,
        allowDuplicates: Boolean = false
    ): Result<VaultFileEntity> {
        _importProgress.value = ImportProgress(status = ImportStatus.VALIDATING)
        try {
            val metadata = metadataReader.readMetadata(uri)
            
            // Validate size and readable input streams
            val validation = validator.validate(uri, metadata.size)
            if (validation.isFailure) {
                val err = validation.exceptionOrNull()?.message ?: "Validation failed"
                _importProgress.value = ImportProgress(status = ImportStatus.FAILED, errorMessage = err)
                return Result.failure(Exception(err))
            }

            // Compute hash
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

            // Check for duplicate imports
            if (!allowDuplicates && fileHash != null) {
                val duplicate = duplicateDetector.findDuplicateByHash(fileHash)
                if (duplicate != null) {
                    val err = "Duplicate file already exists in vault: ${duplicate.name}"
                    _importProgress.value = ImportProgress(status = ImportStatus.FAILED, errorMessage = err)
                    return Result.failure(Exception(err))
                }
            }

            // Encrypt to target location and persist to database
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
                _importProgress.value = ImportProgress(status = ImportStatus.COMPLETED)
                result
            } else {
                val err = result.exceptionOrNull()?.message ?: "Import failed"
                _importProgress.value = ImportProgress(status = ImportStatus.FAILED, errorMessage = err)
                result
            }
        } catch (e: Exception) {
            val err = e.message ?: "Import failed"
            _importProgress.value = ImportProgress(status = ImportStatus.FAILED, errorMessage = err)
            return Result.failure(e)
        }
    }

    /**
     * Batch import multiple files.
     */
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
            val res = importFile(uri, parentId, allowDuplicates)
            results.add(res)
        }
        _importProgress.value = ImportProgress(
            status = ImportStatus.COMPLETED,
            currentFileIndex = total,
            totalFiles = total
        )
        return results
    }

    /**
     * Overloaded import path for pre-resolved metadata (used by Sharesheet imports).
     */
    suspend fun importFile(
        uri: Uri,
        name: String,
        mimeType: String,
        parentId: String? = null,
        fileHash: String? = null
    ): Result<VaultFileEntity> {
        return storageManager.importFile(
            uri = uri,
            fileName = name,
            mimeType = mimeType,
            parentId = parentId,
            fileHash = fileHash
        )
    }
}
