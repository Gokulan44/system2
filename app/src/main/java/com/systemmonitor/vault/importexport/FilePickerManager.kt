package com.systemmonitor.vault.importexport

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilePickerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun resolveUriMetadata(uri: Uri): UriMetadata {
        var name = "Imported_File"
        var sizeBytes = 0L
        var mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

            if (cursor.moveToFirst()) {
                if (nameIndex >= 0) {
                    cursor.getString(nameIndex)?.let { name = it }
                }
                if (sizeIndex >= 0) {
                    sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }

        return UriMetadata(name, sizeBytes, mimeType)
    }

    data class UriMetadata(
        val fileName: String,
        val sizeBytes: Long,
        val mimeType: String
    )
}
