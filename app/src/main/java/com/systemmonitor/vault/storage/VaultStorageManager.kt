package com.systemmonitor.vault.storage

import android.content.Context
import android.net.Uri
import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.importexport.FileHashManager
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
    private val directoryManager: VaultDirectoryManager,
    private val fileManager: VaultFileManager,
    private val secureFileWriter: SecureFileWriter,
    private val secureFileReader: SecureFileReader,
    private val tempFileManager: TempFileManager,
    private val cleanupManager: StorageCleanupManager,
    private val fileHashManager: FileHashManager,
    private val fileDao: VaultFileDao
) {
    suspend fun importFile(
        uri: Uri,
        fileName: String,
        mimeType: String,
        parentId: String?,
        fileHash: String? = null
    ): Result<VaultFileEntity> = withContext(Dispatchers.IO) {
        var tempSourceFile: File? = null
        try {
            val contentResolver = context.contentResolver
            
            // 1. Create temporary source file from Uri
            tempSourceFile = tempFileManager.createTempFile("import_", "_source")
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempSourceFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open Uri input stream"))

            val sizeBytes = tempSourceFile.length()
            val id = UUID.randomUUID().toString()
            val localEncryptedFile = fileManager.createEncryptedFile(id)

            // 2. Encrypt temp file to target location
            secureFileWriter.writeEncryptedFile(tempSourceFile, localEncryptedFile)
            val checksum = try {
                fileHashManager.calculateSha256(localEncryptedFile)
            } catch (e: Exception) {
                null
            }

            // 3. Save details to Room DB
            val entity = VaultFileEntity(
                id = id,
                name = fileName,
                localPath = localEncryptedFile.absolutePath,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                parentId = parentId,
                createdAt = System.currentTimeMillis(),
                fileHash = fileHash,
                checksum = checksum
            )
            fileDao.insertFile(entity)
            Result.success(entity)
        } catch (e: Exception) {
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
