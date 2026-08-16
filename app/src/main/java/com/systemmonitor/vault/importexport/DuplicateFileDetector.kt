package com.systemmonitor.vault.importexport

import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.database.VaultFileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuplicateFileDetector @Inject constructor(
    private val fileDao: VaultFileDao
) {
    suspend fun findDuplicateByHash(fileHash: String): VaultFileEntity? {
        if (fileHash.isBlank()) return null
        return fileDao.getFileByHash(fileHash)
    }

    suspend fun findDuplicateByNameAndSize(fileName: String, sizeBytes: Long): VaultFileEntity? {
        return fileDao.getFileByNameAndSize(fileName, sizeBytes)
    }
}
