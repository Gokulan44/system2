package com.systemmonitor.vault.repository

import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.model.VaultFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultFileRepository @Inject constructor(
    private val fileDao: VaultFileDao
) {
    fun getFilesInFolder(parentId: String?): Flow<List<VaultFile>> {
        return fileDao.getFilesInFolder(parentId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getAllFiles(): Flow<List<VaultFile>> {
        return fileDao.getAllFiles().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getTrashFiles(): Flow<List<VaultFile>> {
        return fileDao.getTrashFiles().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    suspend fun getFileById(id: String): VaultFile? {
        return fileDao.getFileById(id)?.toDomainModel()
    }

    suspend fun getFileByHash(hash: String): VaultFile? {
        return fileDao.getFileByHash(hash)?.toDomainModel()
    }

    suspend fun insertFile(file: VaultFile) {
        fileDao.insertFile(file.toEntity())
    }

    suspend fun renameFile(fileId: String, newName: String) {
        fileDao.renameFile(fileId, newName)
    }

    suspend fun setTrashStatus(fileId: String, isTrash: Boolean) {
        val trashedAt = if (isTrash) System.currentTimeMillis() else null
        fileDao.setTrashStatus(fileId, isTrash, trashedAt)
    }

    suspend fun deleteFileById(fileId: String) {
        fileDao.deleteFileById(fileId)
    }

    private fun VaultFileEntity.toDomainModel() = VaultFile(
        id = id,
        name = name,
        localPath = localPath,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        parentId = parentId,
        createdAt = createdAt,
        fileHash = fileHash,
        checksum = checksum,
        isTrash = isTrash,
        trashedAt = trashedAt
    )

    private fun VaultFile.toEntity() = VaultFileEntity(
        id = id,
        name = name,
        localPath = localPath,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        parentId = parentId,
        createdAt = createdAt,
        fileHash = fileHash,
        checksum = checksum,
        isTrash = isTrash,
        trashedAt = trashedAt
    )
}
