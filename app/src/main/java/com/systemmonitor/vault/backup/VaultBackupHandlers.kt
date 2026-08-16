package com.systemmonitor.vault.backup

import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.database.VaultFolderDao
import com.systemmonitor.vault.storage.VaultDirectoryManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedBackupManager @Inject constructor(
    private val directoryManager: VaultDirectoryManager,
    private val fileDao: VaultFileDao,
    private val folderDao: VaultFolderDao
) {
    suspend fun createBackupArchive(): Result<File> {
        return try {
            val backupFile = File(directoryManager.backupDir, "vault_backup_${System.currentTimeMillis()}.zip")
            ZipOutputStream(FileOutputStream(backupFile)).use { zos ->
                directoryManager.encryptedDir.listFiles()?.forEach { file ->
                    val entry = ZipEntry("files/${file.name}")
                    zos.putNextEntry(entry)
                    FileInputStream(file).use { fis -> fis.copyTo(zos) }
                    zos.closeEntry()
                }
            }
            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class BackupValidator @Inject constructor() {
    fun validateBackupFile(file: File): Boolean {
        return file.exists() && file.length() > 0 && file.name.endsWith(".zip", ignoreCase = true)
    }
}

@Singleton
class VaultBackupManager @Inject constructor(
    val backupManager: EncryptedBackupManager,
    val backupValidator: BackupValidator
)
