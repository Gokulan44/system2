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
        var name = "shared_file"
        var size = 0L
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex) ?: name
                    }
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        return SharedFile(uri = uri, name = name, mimeType = mimeType, size = size)
    }
}
