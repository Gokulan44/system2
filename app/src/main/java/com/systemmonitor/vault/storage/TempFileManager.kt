package com.systemmonitor.vault.storage

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempFileManager @Inject constructor(
    private val directoryManager: VaultDirectoryManager
) {
    fun createTempFile(prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, directoryManager.tempDir)
    }

    fun getTempFile(fileName: String): File {
        return File(directoryManager.tempDir, fileName)
    }

    fun deleteTempFile(file: File): Boolean {
        return if (file.exists()) file.delete() else true
    }

    fun clearAllTempFiles() {
        directoryManager.clearTempDir()
    }
}
