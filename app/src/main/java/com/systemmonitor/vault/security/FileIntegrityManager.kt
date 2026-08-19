package com.systemmonitor.vault.security

import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.storage.SecureFileReader
import com.systemmonitor.vault.storage.TempFileManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class FileIntegrityManager @Inject constructor(
    private val fileDao: VaultFileDao,
    private val fileHashManager: FileHashManager,
    private val secureFileReader: SecureFileReader,
    private val tempFileManager: TempFileManager
) {
    suspend fun verifyIntegrity(fileId: String): IntegrityResult = withContext(Dispatchers.IO) {
        val entity = fileDao.getFileById(fileId)
            ?: return@withContext IntegrityResult.Corrupted("File not found in database")

        val file = File(entity.localPath)
        if (!file.exists()) {
            return@withContext IntegrityResult.Corrupted("Encrypted file missing from storage")
        }

        val expectedChecksum = entity.checksum
        if (!expectedChecksum.isNull_or_blank()) {
            val currentChecksum = try {
                fileHashManager.calculateSha256(file)
            } catch (e: Exception) {
                return@withContext IntegrityResult.Corrupted("Failed to calculate SHA-256: ${e.message}")
            }
            return@withContext if (currentChecksum == expectedChecksum) {
                IntegrityResult.Valid("Checksum match confirmed")
            } else {
                IntegrityResult.Corrupted("SHA-256 checksum mismatch (encrypted file)")
            }
        }

        val expectedHash = entity.fileHash
        if (expectedHash.isNull_or_blank()) {
            return@withContext IntegrityResult.Valid("No stored hash to verify against")
        }

        // Fallback: Decrypt and verify legacy files
        val tempDecryptedFile = try {
            tempFileManager.createTempFile("integrity_", "_decrypted")
        } catch (e: Exception) {
            return@withContext IntegrityResult.Corrupted("Failed to create temporary verification file: ${e.message}")
        }

        try {
            secureFileReader.readDecryptedFile(file, tempDecryptedFile)
            val currentFileHash = fileHashManager.calculateSha256(tempDecryptedFile)

            if (currentFileHash == expectedHash) {
                val encryptedHash = fileHashManager.calculateSha256(file)
                fileDao.insertFile(entity.copy(checksum = encryptedHash))
                IntegrityResult.Valid("Checksum match confirmed via legacy fallback (cached)")
            } else {
                IntegrityResult.Corrupted("SHA-256 checksum mismatch on legacy file")
            }
        } catch (e: Exception) {
            IntegrityResult.Corrupted("Decryption failed: ${e.message}")
        } finally {
            tempFileManager.deleteTempFile(tempDecryptedFile)
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

    sealed class IntegrityResult {
        data class Valid(val message: String) : IntegrityResult()
        data class Corrupted(val reason: String) : IntegrityResult()
    }
}
