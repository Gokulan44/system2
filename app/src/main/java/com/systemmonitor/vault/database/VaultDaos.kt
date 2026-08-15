package com.systemmonitor.vault.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultFolderDao {
    @Query("SELECT * FROM vault_folders WHERE parentId IS :parentId ORDER BY name ASC")
    fun getFoldersInFolder(parentId: String?): Flow<List<VaultFolderEntity>>

    @Query("SELECT * FROM vault_folders WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: String): VaultFolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: VaultFolderEntity)

    @Query("UPDATE vault_folders SET name = :newName WHERE id = :id")
    suspend fun renameFolder(id: String, newName: String)

    @Delete
    suspend fun deleteFolder(folder: VaultFolderEntity)

    @Query("DELETE FROM vault_folders WHERE id = :id")
    suspend fun deleteFolderById(id: String)
}

@Dao
interface VaultFileDao {
    @Query("SELECT * FROM vault_files WHERE parentId IS :parentId ORDER BY name ASC")
    fun getFilesInFolder(parentId: String?): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files ORDER BY createdAt DESC")
    fun getAllFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): VaultFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFileEntity)

    @Query("UPDATE vault_files SET name = :newName WHERE id = :id")
    suspend fun renameFile(id: String, newName: String)

    @Delete
    suspend fun deleteFile(file: VaultFileEntity)

    @Query("DELETE FROM vault_files WHERE id = :id")
    suspend fun deleteFileById(id: String)
    
    @Query("DELETE FROM vault_files WHERE parentId = :parentId")
    suspend fun deleteFilesByParentId(parentId: String)
}

@Dao
interface VaultAuditDao {
    @Query("SELECT * FROM vault_audit_logs ORDER BY timestamp DESC")
    fun getAllAudits(): Flow<List<VaultAuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: VaultAuditEntity)

    @Query("DELETE FROM vault_audit_logs")
    suspend fun clearAllAudits()
}
