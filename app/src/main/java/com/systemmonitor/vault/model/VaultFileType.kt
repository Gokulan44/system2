package com.systemmonitor.vault.model

import java.util.Locale

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
            return fromMimeTypeAndName(mime, null)
        }

        fun fromMimeTypeAndName(mime: String?, name: String?): VaultFileType {
            val lowerMime = mime?.lowercase(Locale.getDefault()) ?: ""
            val ext = name?.substringAfterLast('.', "")?.lowercase(Locale.getDefault()) ?: ""

            return when {
                lowerMime.startsWith("image/") || ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif", "svg", "tiff", "raw", "dng", "ico") -> IMAGE
                lowerMime.startsWith("video/") || ext in listOf("mp4", "mkv", "mov", "avi", "webm", "3gp", "flv", "m4v", "ts") -> VIDEO
                lowerMime.startsWith("audio/") || ext in listOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus", "amr") -> AUDIO
                lowerMime.contains("pdf") || lowerMime.contains("doc") ||
                lowerMime.contains("txt") || lowerMime.contains("xls") ||
                lowerMime.contains("ppt") || lowerMime.contains("text/") ||
                ext in listOf("pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx", "ppt", "pptx", "csv", "json", "log", "md", "html", "xml") -> DOCUMENT
                lowerMime.contains("zip") || lowerMime.contains("rar") ||
                lowerMime.contains("tar") || lowerMime.contains("7z") ||
                lowerMime.contains("compressed") || ext in listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz") -> ARCHIVE
                lowerMime.contains("android.package-archive") || ext == "apk" -> APK
                else -> OTHER
            }
        }
    }
}
