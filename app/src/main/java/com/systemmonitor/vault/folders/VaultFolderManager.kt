package com.systemmonitor.vault.folders

import com.systemmonitor.vault.database.VaultFolderDao
import com.systemmonitor.vault.database.VaultFolderEntity
import com.systemmonitor.vault.model.VaultFolder
import com.systemmonitor.vault.repository.VaultFolderRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderTree @Inject constructor(
    private val folderDao: VaultFolderDao
) {
    suspend fun buildBreadcrumbPath(folderId: String?): List<VaultFolder> {
        val breadcrumbs = mutableListOf<VaultFolder>()
        var tempId = folderId
        while (tempId != null) {
            val entity = folderDao.getFolderById(tempId)
            if (entity != null) {
                breadcrumbs.add(0, VaultFolder(entity.id, entity.name, entity.parentId, entity.createdAt, entity.isTrash))
                tempId = entity.parentId
            } else {
                tempId = null
            }
        }
        return breadcrumbs
    }
}

@Singleton
class FolderOperations @Inject constructor(
    private val folderRepository: VaultFolderRepository
) {
    suspend fun createFolder(name: String, parentId: String?): VaultFolder {
        val folder = VaultFolder(
            id = UUID.randomUUID().toString(),
            name = name,
            parentId = parentId,
            createdAt = System.currentTimeMillis()
        )
        folderRepository.insertFolder(folder)
        return folder
    }

    suspend fun renameFolder(folderId: String, newName: String) {
        folderRepository.renameFolder(folderId, newName)
    }

    suspend fun deleteFolder(folderId: String) {
        folderRepository.deleteFolderById(folderId)
    }
}

@Singleton
class VaultFolderManager @Inject constructor(
    val folderTree: FolderTree,
    val folderOperations: FolderOperations,
    val folderRepository: VaultFolderRepository
)
