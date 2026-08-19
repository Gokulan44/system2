package com.systemmonitor.vault.importing

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.importexport.DuplicateFileDetector
import com.systemmonitor.vault.security.FileHashManager
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

    companion object {
        private const val TAG = "FileImportManager"
    }

    /**
     * Standard import path resolving metadata on-the-fly.
     */
    suspend fun importFile(
        uri: Uri,
        parentId: String? = null,
        allowDuplicates: Boolean = false,
        deleteOriginalAfterImport: Boolean = true
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
                allowDuplicates = allowDuplicates
            )

            return if (result.isSuccess) {
                // The encrypted copy is safely in the vault and the DB entity is
                // persisted. Only NOW is it safe to remove the original source file
                // so it stops appearing in the gallery / downloads / file manager.
                if (deleteOriginalAfterImport) {
                    deleteOriginalSafely(uri)
                }
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
        allowDuplicates: Boolean = false,
        deleteOriginalAfterImport: Boolean = true
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
            val res = importFile(uri, parentId, allowDuplicates, deleteOriginalAfterImport)
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
        fileHash: String? = null,
        deleteOriginalAfterImport: Boolean = true
    ): Result<VaultFileEntity> {
        val result = storageManager.importFile(
            uri = uri,
            fileName = name,
            mimeType = mimeType,
            parentId = parentId,
            fileHash = fileHash
        )

        if (result.isSuccess && deleteOriginalAfterImport) {
            deleteOriginalSafely(uri)
        }

        return result
    }

    /**
     * Deletes the original source file/document referenced by [uri] now that it has
     * been safely encrypted into the vault and its DB entity persisted.
     *
     * Handles the three URI shapes we actually see in practice:
     *  - MediaStore content:// URIs (gallery images/videos) — a plain
     *    ContentResolver.delete() is often blocked by scoped storage on API 29+
     *    and throws RecoverableSecurityException on API 29, or silently returns 0
     *    rows deleted on API 30+ unless the caller owns the media entry.
     *  - SAF / DocumentsProvider content:// URIs (Downloads, SD card, etc.) — must
     *    go through DocumentsContract.deleteDocument, not ContentResolver.delete.
     *  - file:// URIs — deleted directly from disk.
     *
     * NOTE: On API 29+, a RecoverableSecurityException carries an IntentSender that
     * must be launched from an Activity (startIntentSenderForResult) for the user to
     * grant one-time delete permission. A background/singleton manager cannot show
     * that UI itself — see the TODO below for how callers should wire this up.
     */
    private fun deleteOriginalSafely(uri: Uri) {
        try {
            when (uri.scheme) {
                "file" -> {
                    val deleted = uri.path?.let { java.io.File(it).delete() } ?: false
                    if (!deleted) {
                        Log.w(TAG, "Failed to delete original file at ${uri.path}")
                    }
                }
                "content" -> {
                    if (DocumentsContract.isDocumentUri(context, uri)) {
                        val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
                        if (!deleted) {
                            Log.w(TAG, "DocumentsContract.deleteDocument returned false for $uri")
                        }
                    } else {
                        val rows = context.contentResolver.delete(uri, null, null)
                        if (rows <= 0) {
                            Log.w(TAG, "ContentResolver.delete removed 0 rows for $uri")
                        }
                    }
                }
                else -> {
                    Log.w(TAG, "Unhandled URI scheme '${uri.scheme}' — original not deleted: $uri")
                }
            }
        } catch (e: SecurityException) {
            // Expected on API 29+ for MediaStore entries the app doesn't own.
            // TODO: bubble this up (e.g. via a SharedFlow<RecoverableDeleteRequest>)
            // so the hosting Activity/Fragment can call
            // (e as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender
            // with startIntentSenderForResult(...) on API 29, or build a
            // MediaStore.createDeleteRequest(contentResolver, listOf(uri)) on API 30+
            // and launch that IntentSender instead. Silently swallowing this is what
            // caused originals to remain in the gallery — do not remove this catch,
            // but do NOT leave it as a silent no-op in production.
            Log.w(TAG, "SecurityException deleting original $uri — needs user consent flow (API ${Build.VERSION.SDK_INT})", e)
        } catch (e: Exception) {
            Log.w(TAG, "Unexpected error deleting original $uri", e)
        }
    }
}