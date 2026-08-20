package com.systemmonitor.vault.importing

import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileImportValidator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024 * 1024 // 2GB limit
        private const val TAG = "FileImportValidator"
    }

    /**
     * Validates size limits and free storage space WITHOUT opening the URI's
     * InputStream.
     *
     * IMPORTANT: this used to open+immediately close contentResolver.openInputStream(uri)
     * as an "is it openable" check. That is unsafe for some content:// providers
     * (certain camera FileProviders, some third-party share/gallery pickers) whose
     * data is backed by a single-use pipe — opening the stream once, even just to
     * close it right away, can exhaust that pipe so the REAL read later (in
     * VaultStorageManager) gets an already-empty stream and silently produces a
     * 0-byte imported file with no exception thrown anywhere.
     *
     * The actual "can we read this" check now happens exactly once, at the one
     * place that does the real copy (VaultStorageManager.importFileInternal),
     * which is also where the size is verified against the copied byte count.
     */
    fun validate(uri: Uri, size: Long): Result<Boolean> {
        if (size > MAX_FILE_SIZE_BYTES) {
            return Result.failure(Exception("File size exceeds 2GB maximum limit"))
        }

        // Non-destructive existence/readability check via query instead of
        // opening a stream. Not every provider supports this query, so a
        // failure here is logged but not treated as fatal — the real,
        // authoritative check happens when VaultStorageManager actually
        // copies the bytes.
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.count == 0) {
                    Log.w(TAG, "Query returned 0 rows for $uri — provider may not support query(), continuing anyway")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Non-fatal: unable to query $uri before import: ${e.message}")
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