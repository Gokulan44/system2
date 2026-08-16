package com.systemmonitor.vault.trash

import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.database.VaultFolderDao
import com.systemmonitor.vault.repository.VaultAuditRepository
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureDeleteManager @Inject constructor() {
    fun shredFile(file: File) {
        if (!file.exists()) return
        try {
            val length = file.length()
            RandomAccessFile(file, "rws").use { raf ->
                val overwriteBuffer = ByteArray(4096)
                var written = 0L
                while (written < length) {
                    val toWrite = minOf(overwriteBuffer.size.toLong(), length - written).toInt()
                    raf.write(overwriteBuffer, 0, toWrite)
                    written += toWrite
                }
            }
        } catch (e: Exception) {
            // Fallback to standard delete if shredding throws
        } finally {
            file.delete()
        }
    }
}

@Singleton
class VaultTrashManager @Inject constructor(
    private val fileDao: VaultFileDao,
    private val folderDao: VaultFolderDao,
    private val secureDeleteManager: SecureDeleteManager,
    private val auditRepository: VaultAuditRepository
) {
    suspend fun moveToTrash(fileId: String) {
        val entity = fileDao.getFileById(fileId) ?: return
        fileDao.setTrashStatus(fileId, isTrash = true, trashedAt = System.currentTimeMillis())
        auditRepository.logEvent("TRASH_MOVE", "Moved file '${entity.name}' to Trash Bin")
    }

    suspend fun restoreFromTrash(fileId: String) {
        val entity = fileDao.getFileById(fileId) ?: return
        fileDao.setTrashStatus(fileId, isTrash = false, trashedAt = null)
        auditRepository.logEvent("TRASH_RESTORE", "Restored file '${entity.name}' from Trash Bin")
    }

    suspend fun permanentlyDelete(fileId: String) {
        val entity = fileDao.getFileById(fileId) ?: return
        secureDeleteManager.shredFile(File(entity.localPath))
        fileDao.deleteFileById(fileId)
        auditRepository.logEvent("TRASH_SHRED", "Permanently shredded file '${entity.name}'")
    }

    suspend fun emptyTrash() {
        val trashFiles = fileDao.getTrashFilesList()
        trashFiles.forEach { entity ->
            secureDeleteManager.shredFile(File(entity.localPath))
            fileDao.deleteFileById(entity.id)
        }
        auditRepository.logEvent("TRASH_EMPTY", "Emptied ${trashFiles.size} items from Trash Bin")
    }
}
