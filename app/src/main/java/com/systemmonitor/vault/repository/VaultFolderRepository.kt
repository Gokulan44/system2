package com.systemmonitor.vault.repository

import com.systemmonitor.vault.database.VaultFolderDao
import com.systemmonitor.vault.database.VaultFolderEntity
import com.systemmonitor.vault.model.VaultFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultFolderRepository @Inject constructor(
    private val folderDao: VaultFolderDao
) {
    fun getFoldersInFolder(parentId: String?): Flow<List<VaultFolder>> {
        return folderDao.getFoldersInFolder(parentId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    suspend fun getFolderById(id: String): VaultFolder? {
        return folderDao.getFolderById(id)?.toDomainModel()
    }

    suspend fun insertFolder(folder: VaultFolder) {
        folderDao.insertFolder(folder.toEntity())
    }

    suspend fun renameFolder(folderId: String, newName: String) {
        folderDao.renameFolder(folderId, newName)
    }

    suspend fun setTrashStatus(folderId: String, isTrash: Boolean) {
        folderDao.setTrashStatus(folderId, isTrash)
    }

    suspend fun deleteFolderById(folderId: String) {
        folderDao.deleteFolderById(folderId)
    }

    private fun VaultFolderEntity.toDomainModel() = VaultFolder(
        id = id,
        name = name,
        parentId = parentId,
        createdAt = createdAt,
        isTrash = isTrash
    )

    private fun VaultFolder.toEntity() = VaultFolderEntity(
        id = id,
        name = name,
        parentId = parentId,
        createdAt = createdAt,
        isTrash = isTrash
    )
}
