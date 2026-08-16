package com.systemmonitor.vault.model

enum class VaultFileType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    ARCHIVE,
    APK,
    OTHER;

    companion object {
        fun fromMimeType(mime: String?): VaultFileType {
            if (mime == null) return OTHER
            val lowerMime = mime.lowercase()
            return when {
                lowerMime.startsWith("image/") -> IMAGE
                lowerMime.startsWith("video/") -> VIDEO
                lowerMime.startsWith("audio/") -> AUDIO
                lowerMime.contains("pdf") || lowerMime.contains("doc") ||
                lowerMime.contains("txt") || lowerMime.contains("xls") ||
                lowerMime.contains("ppt") || lowerMime.contains("text/") -> DOCUMENT
                lowerMime.contains("zip") || lowerMime.contains("rar") ||
                lowerMime.contains("tar") || lowerMime.contains("7z") ||
                lowerMime.contains("compressed") -> ARCHIVE
                lowerMime.contains("android.package-archive") || lowerMime.endsWith(".apk") -> APK
                else -> OTHER
            }
        }
    }
}
