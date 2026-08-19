package com.systemmonitor.vault.importing

import android.content.Context
import android.net.Uri
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileImportValidator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024 * 1024 // 2GB limit
    }

    fun validate(uri: Uri, size: Long): Result<Boolean> {
        if (size > MAX_FILE_SIZE_BYTES) {
            return Result.failure(Exception("File size exceeds 2GB maximum limit"))
        }

        try {
            context.contentResolver.openInputStream(uri)?.close() ?: 
                return Result.failure(Exception("Unable to open URI input stream"))
        } catch (e: Exception) {
            return Result.failure(Exception("URI validation failed: ${e.message}"))
        }

        val internalDir = context.filesDir
        val stat = StatFs(internalDir.path)
        val freeBytes = stat.availableBytes
        if (freeBytes < 50 * 1024 * 1024L) { // 50MB free min limit
            return Result.failure(Exception("Insufficient storage space on device"))
        }

        return Result.success(true)
    }
}
