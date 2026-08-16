package com.systemmonitor.vault.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_folders")
data class VaultFolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String?,
    val createdAt: Long,
    val isTrash: Boolean = false
)

@Entity(tableName = "vault_files")
data class VaultFileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val localPath: String,
    val mimeType: String,
    val sizeBytes: Long,
    val parentId: String?,
    val createdAt: Long,
    val fileHash: String? = null,
    val checksum: String? = null,
    val isTrash: Boolean = false,
    val trashedAt: Long? = null
)

@Entity(tableName = "vault_audit_logs")
data class VaultAuditEntity(
    @PrimaryKey val id: String,
    val action: String,
    val details: String,
    val timestamp: Long
)

@Entity(tableName = "vault_settings")
data class VaultSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
