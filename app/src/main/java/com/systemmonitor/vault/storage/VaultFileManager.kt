package com.systemmonitor.vault.storage

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultFileManager @Inject constructor(
    private val directoryManager: VaultPathManager
) {
    fun createEncryptedFile(fileId: String): File {
        return File(directoryManager.encryptedDir, fileId)
    }

    fun getEncryptedFile(fileId: String): File? {
        val file = File(directoryManager.encryptedDir, fileId)
        return if (file.exists()) file else null
    }

    fun deleteEncryptedFile(fileId: String): Boolean {
        val file = File(directoryManager.encryptedDir, fileId)
        return if (file.exists()) file.delete() else true
    }

    fun calculateTotalEncryptedSize(): Long {
        return directoryManager.encryptedDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}
