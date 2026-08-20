package com.systemmonitor.vault.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultFolderDao {
    @Query("SELECT * FROM vault_folders WHERE parentId IS :parentId AND isTrash = 0 ORDER BY name ASC")
    fun getFoldersInFolder(parentId: String?): Flow<List<VaultFolderEntity>>

    @Query("SELECT * FROM vault_folders WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: String): VaultFolderEntity?

    @Query("SELECT * FROM vault_folders WHERE isTrash = 0")
    suspend fun getAllFoldersList(): List<VaultFolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: VaultFolderEntity)

    @Query("UPDATE vault_folders SET name = :newName WHERE id = :id")
    suspend fun renameFolder(id: String, newName: String)

    @Query("UPDATE vault_folders SET isTrash = :isTrash WHERE id = :id")
    suspend fun setTrashStatus(id: String, isTrash: Boolean)

    @Delete
    suspend fun deleteFolder(folder: VaultFolderEntity)

    @Query("DELETE FROM vault_folders WHERE id = :id")
    suspend fun deleteFolderById(id: String)
}

@Dao
interface VaultFileDao {
    @Query("SELECT * FROM vault_files WHERE parentId IS :parentId AND isTrash = 0 ORDER BY name ASC")
    fun getFilesInFolder(parentId: String?): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isTrash = 0 ORDER BY createdAt DESC")
    fun getAllFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isTrash = 0")
    suspend fun getAllFilesList(): List<VaultFileEntity>

    @Query("SELECT * FROM vault_files WHERE isTrash = 1")
    fun getTrashFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isTrash = 1")
    suspend fun getTrashFilesList(): List<VaultFileEntity>

    @Query("SELECT * FROM vault_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): VaultFileEntity?

    @Query("SELECT * FROM vault_files WHERE fileHash = :hash LIMIT 1")
    suspend fun getFileByHash(hash: String): VaultFileEntity?

    @Query("SELECT * FROM vault_files WHERE name = :name AND sizeBytes = :size LIMIT 1")
    suspend fun getFileByNameAndSize(name: String, size: Long): VaultFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFileEntity)

    @Query("UPDATE vault_files SET name = :newName WHERE id = :id")
    suspend fun renameFile(id: String, newName: String)

    @Query("UPDATE vault_files SET isTrash = :isTrash, trashedAt = :trashedAt WHERE id = :id")
    suspend fun setTrashStatus(id: String, isTrash: Boolean, trashedAt: Long?)

    @Delete
    suspend fun deleteFile(file: VaultFileEntity)

    @Query("DELETE FROM vault_files WHERE id = :id")
    suspend fun deleteFileById(id: String)
    
    @Query("DELETE FROM vault_files WHERE parentId = :parentId")
    suspend fun deleteFilesByParentId(parentId: String)

    @Query("DELETE FROM vault_files")
    suspend fun deleteAllFiles()
}

@Dao
interface VaultAuditDao {
    @Query("SELECT * FROM vault_audit_logs ORDER BY timestamp DESC")
    fun getAllAudits(): Flow<List<VaultAuditEntity>>

    @Query("SELECT * FROM vault_audit_logs ORDER BY timestamp DESC")
    suspend fun getAllAuditsList(): List<VaultAuditEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: VaultAuditEntity)

    @Query("DELETE FROM vault_audit_logs")
    suspend fun clearAllAudits()
}

@Dao
interface VaultSettingsDao {
    @Query("SELECT value FROM vault_settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: VaultSettingsEntity)

    @Query("DELETE FROM vault_settings WHERE key = :key")
    suspend fun deleteSetting(key: String)
}
