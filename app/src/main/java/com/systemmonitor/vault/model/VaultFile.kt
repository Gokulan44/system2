package com.systemmonitor.vault.model

data class VaultFile(
    val id: String,
    val name: String,
    val localPath: String,
    val mimeType: String,
    val sizeBytes: Long,
    val parentId: String?,
    val createdAt: Long,
    val fileHash: String? = null,
    val checksum: String? = null,
    val isEncrypted: Boolean = true,
    val isTrash: Boolean = false,
    val trashedAt: Long? = null,
    val fileType: VaultFileType = VaultFileType.fromMimeType(mimeType)
)
