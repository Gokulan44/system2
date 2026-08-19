package com.systemmonitor.vault.importing

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ImportFileMetadata(
    val fileName: String,
    val mimeType: String,
    val size: Long
)

@Singleton
class FileMetadataReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileTypeDetector: FileTypeDetector
) {
    fun readMetadata(uri: Uri): ImportFileMetadata {
        val contentResolver = context.contentResolver
        var name = "unnamed_file"
        var size = -1L
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

        return ImportFileMetadata(fileName = name, mimeType = mimeType, size = size)
    }
}
