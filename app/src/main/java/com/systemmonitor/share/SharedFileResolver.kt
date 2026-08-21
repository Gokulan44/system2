package com.systemmonitor.share

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedFileResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun resolveSharedFile(uri: Uri): SharedFile? {
        val contentResolver = context.contentResolver
        var mimeType = contentResolver.getType(uri)
        
        var name = when {
            mimeType?.startsWith("image/") == true -> "shared_image_${System.currentTimeMillis()}"
            mimeType?.startsWith("video/") == true -> "shared_video_${System.currentTimeMillis()}"
            else -> "shared_file_${System.currentTimeMillis()}"
        }
        
        var size = 0L

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        val resolvedName = cursor.getString(nameIndex)
                        if (!resolvedName.isNullOrBlank()) {
                            name = resolvedName
                        }
                    }
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        // If MIME is still unknown, try to guess from name/extension
        if (mimeType == null || mimeType == "application/octet-stream") {
            val extension = name.substringAfterLast('.', "").lowercase()
            if (extension.isNotEmpty()) {
                mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
        }
        
        val finalMime = mimeType ?: "application/octet-stream"

        return SharedFile(uri = uri, name = name, mimeType = finalMime, size = size)
    }
}
