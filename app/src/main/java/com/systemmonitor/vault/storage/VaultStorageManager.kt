package com.systemmonitor.vault.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.security.FileHashManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultStorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val directoryManager: VaultPathManager,
    private val fileManager: VaultFileManager,
    private val secureFileWriter: SecureFileWriter,
    private val secureFileReader: SecureFileReader,
    private val tempFileManager: TempFileManager,
    private val cleanupManager: StorageCleanupManager,
    private val fileHashManager: FileHashManager,
    private val fileDao: VaultFileDao
) {
    companion object {
        private const val TAG = "VaultStorageManager"
    }

    suspend fun importFile(
        uri: Uri,
        fileName: String,
        mimeType: String,
        parentId: String?,
        allowDuplicates: Boolean = false
    ): Result<VaultFileEntity> {
        return importFileInternal(uri, fileName, mimeType, parentId, fileHash = null, allowDuplicates = allowDuplicates)
    }

    suspend fun importFile(
        uri: Uri,
        fileName: String,
        mimeType: String,
        parentId: String?,
        fileHash: String?
    ): Result<VaultFileEntity> {
        return importFileInternal(uri, fileName, mimeType, parentId, fileHash = fileHash, allowDuplicates = true)
    }

    /**
     * Copies the URI's content into [tempSourceFile], returning the number of
     * bytes actually copied. Exists as its own function so it can be retried:
     * some content:// providers back their data with a one-shot pipe, and a
     * previous open elsewhere (e.g. a validator's openInputStream/close check)
     * can leave that pipe exhausted, producing a "successful" copy of 0 bytes
     * with no exception. A fresh openInputStream() call on retry gets a new
     * pipe from the provider and reliably succeeds in that case.
     */
    private fun copyUriToFile(uri: Uri, tempSourceFile: File): Long {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempSourceFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw Exception("Failed to open Uri input stream")
        return tempSourceFile.length()
    }

    private suspend fun importFileInternal(
        uri: Uri,
        fileName: String,
        mimeType: String,
        parentId: String?,
        fileHash: String?,
        allowDuplicates: Boolean
    ): Result<VaultFileEntity> = withContext(Dispatchers.IO) {
        var tempSourceFile: File? = null
        try {
            // 1. Create temporary source file from Uri (OPEN URI STREAM EXACTLY ONCE
            // per attempt — but allow ONE retry if the first attempt copies 0 bytes,
            // since that's a known symptom of a single-use content:// pipe having
            // already been opened/closed elsewhere before this point).
            tempSourceFile = tempFileManager.createTempFile("import_", "_source")

            var sizeBytes = try {
                copyUriToFile(uri, tempSourceFile)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open/copy Uri stream for $uri (scheme=${uri.scheme}, authority=${uri.authority})", e)
                return@withContext Result.failure(Exception("Failed to open Uri input stream"))
            }

            if (sizeBytes <= 0) {
                Log.w(TAG, "First copy attempt produced 0 bytes for $uri (authority=${uri.authority}) — retrying once")
                sizeBytes = try {
                    copyUriToFile(uri, tempSourceFile)
                } catch (e: Exception) {
                    Log.e(TAG, "Retry copy also failed for $uri", e)
                    return@withContext Result.failure(Exception("Failed to open Uri input stream"))
                }
            }

            if (sizeBytes <= 0) {
                Log.e(TAG, "Import produced 0 bytes for $uri after retry (scheme=${uri.scheme}, authority=${uri.authority}, path=${uri.path}) — provider likely does not support being read twice, or genuinely returned an empty file")
                return@withContext Result.failure(Exception("Imported file is empty (0 bytes)"))
            }

            // 2. Resolve original hash from local temp file
            val finalHash = fileHash ?: try {
                tempSourceFile.inputStream().use {
                    fileHashManager.calculateSha256(it)
                }
            } catch (e: Exception) {
                null
            }

            // 3. Duplicate check
            if (!allowDuplicates && finalHash != null) {
                val duplicate = fileDao.getFileByHash(finalHash)
                if (duplicate != null) {
                    return@withContext Result.failure(Exception("Duplicate file already exists in vault: ${duplicate.name}"))
                }
            }

            val id = UUID.randomUUID().toString()
            val localEncryptedFile = fileManager.createEncryptedFile(id)

            // 4. Encrypt temp file to target location
            secureFileWriter.writeEncryptedFile(tempSourceFile, localEncryptedFile)
            val checksum = try {
                fileHashManager.calculateSha256(localEncryptedFile)
            } catch (e: Exception) {
                null
            }

            // 5. Save details to Room DB
            val entity = VaultFileEntity(
                id = id,
                name = fileName,
                localPath = localEncryptedFile.absolutePath,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                parentId = parentId,
                createdAt = System.currentTimeMillis(),
                fileHash = finalHash,
                checksum = checksum
            )
            fileDao.insertFile(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Import failed for $uri", e)
            Result.failure(e)
        } finally {
            tempSourceFile?.let { tempFileManager.deleteTempFile(it) }
        }
    }

    suspend fun exportFile(
        fileId: String,
        outputUri: Uri
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        var tempDecryptedFile: File? = null
        try {
            val entity = fileDao.getFileById(fileId) ?: return@withContext Result.failure(Exception("File not found in database"))
            val encryptedFile = File(entity.localPath)
            if (!encryptedFile.exists()) {
                return@withContext Result.failure(Exception("Encrypted file missing on disk"))
            }

            // 1. Decrypt to temporary file
            tempDecryptedFile = tempFileManager.createTempFile("export_", "_decrypted")
            secureFileReader.readDecryptedFile(encryptedFile, tempDecryptedFile)

            // 2. Write to target Uri
            val contentResolver = context.contentResolver
            contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                tempDecryptedFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open output stream"))

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            tempDecryptedFile?.let { tempFileManager.deleteTempFile(it) }
        }
    }

    suspend fun createTempDecryptedFile(fileId: String): File? = withContext(Dispatchers.IO) {
        try {
            val entity = fileDao.getFileById(fileId) ?: return@withContext null
            val encryptedFile = File(entity.localPath)
            if (!encryptedFile.exists()) return@withContext null

            val extension = File(entity.name).extension.let { if (it.isNotEmpty()) ".$it" else "" }
            val tempFile = File(directoryManager.tempDir, "${fileId}_temp$extension")
            if (tempFile.exists()) tempFile.delete()

            secureFileReader.readDecryptedFile(encryptedFile, tempFile)
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    fun cleanupTempFiles() {
        tempFileManager.clearAllTempFiles()
    }

    suspend fun performStorageCleanup() = cleanupManager.performCleanup()
}