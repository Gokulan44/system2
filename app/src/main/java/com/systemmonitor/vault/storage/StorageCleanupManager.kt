package com.systemmonitor.vault.storage

import com.systemmonitor.vault.database.VaultFileDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageCleanupManager @Inject constructor(
    private val directoryManager: VaultPathManager,
    private val tempFileManager: TempFileManager,
    private val fileDao: VaultFileDao
) {
    suspend fun performCleanup(): CleanUpResult = withContext(Dispatchers.IO) {
        var tempFilesDeleted = 0
        var orphanedFilesDeleted = 0

        // 1. Delete temp files
        directoryManager.tempDir.listFiles()?.forEach { file ->
            if (file.deleteRecursively()) {
                tempFilesDeleted++
            }
        }

        // 2. Delete orphaned files in encrypted storage not listed in DB
        val dbFiles = fileDao.getAllFilesList().map { File(it.localPath).name }.toSet()
        directoryManager.encryptedDir.listFiles()?.forEach { encryptedFile ->
            if (!dbFiles.contains(encryptedFile.name)) {
                if (encryptedFile.delete()) {
                    orphanedFilesDeleted++
                }
            }
        }

        CleanUpResult(tempFilesDeleted, orphanedFilesDeleted)
    }

    data class CleanUpResult(
        val tempFilesCleaned: Int,
        val orphanedFilesCleaned: Int
    )
}
