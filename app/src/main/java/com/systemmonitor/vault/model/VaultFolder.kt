package com.systemmonitor.vault.model

data class VaultFolder(
    val id: String,
    val name: String,
    val parentId: String?,
    val createdAt: Long,
    val isTrash: Boolean = false,
    val itemCount: Int = 0
)
