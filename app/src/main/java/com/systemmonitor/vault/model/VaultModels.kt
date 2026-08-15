package com.systemmonitor.vault.model

enum class VaultFileType {
    IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER;
    
    companion object {
        fun fromMimeType(mime: String?): VaultFileType {
            if (mime == null) return OTHER
            val lowerMime = mime.lowercase()
            return when {
                lowerMime.startsWith("image/") -> IMAGE
                lowerMime.startsWith("video/") -> VIDEO
                lowerMime.startsWith("audio/") -> AUDIO
                lowerMime.contains("pdf") || lowerMime.contains("doc") || lowerMime.contains("txt") || lowerMime.contains("xls") || lowerMime.contains("ppt") || lowerMime.contains("text/") -> DOCUMENT
                else -> OTHER
            }
        }
    }
}

data class VaultFolder(
    val id: String,
    val name: String,
    val parentId: String?,
    val createdAt: Long
)

data class VaultFile(
    val id: String,
    val name: String,
    val localPath: String,
    val mimeType: String,
    val sizeBytes: Long,
    val parentId: String?,
    val createdAt: Long,
    val fileType: VaultFileType = VaultFileType.fromMimeType(mimeType)
)
