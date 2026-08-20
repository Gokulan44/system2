package com.systemmonitor.vault.importing

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OriginalFileCleaner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "OriginalFileCleaner"
    }

    /**
     * Deletes the original source file/document referenced by [uri] now that it has
     * been safely encrypted into the vault and its DB entity persisted.
     *
     * Handles the three URI shapes we actually see in practice:
     *  - MediaStore content:// URIs (gallery images/videos) — a plain
     *    ContentResolver.delete() is often blocked by scoped storage on API 29+
     *    and throws RecoverableSecurityException on API 29, or silently returns 0
     *    rows deleted on API 30+ unless the caller owns the media entry.
     *  - SAF / DocumentsProvider content:// URIs (Downloads, SD card, etc.) — must
     *    go through DocumentsContract.deleteDocument, not ContentResolver.delete.
     *  - file:// URIs — deleted directly from disk.
     */
    fun deleteOriginalSafely(uri: Uri) {
        try {
            when (uri.scheme) {
                "file" -> {
                    val deleted = uri.path?.let { java.io.File(it).delete() } ?: false
                    if (!deleted) {
                        Log.w(TAG, "Failed to delete original file at ${uri.path}")
                    }
                }
                "content" -> {
                    if (DocumentsContract.isDocumentUri(context, uri)) {
                        val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
                        if (!deleted) {
                            Log.w(TAG, "DocumentsContract.deleteDocument returned false for $uri")
                        }
                    } else {
                        val rows = context.contentResolver.delete(uri, null, null)
                        if (rows <= 0) {
                            Log.w(TAG, "ContentResolver.delete removed 0 rows for $uri")
                        }
                    }
                }
                else -> {
                    Log.w(TAG, "Unhandled URI scheme '${uri.scheme}' — original not deleted: $uri")
                }
            }
        } catch (e: SecurityException) {
            // Expected on API 29+ for MediaStore entries the app doesn't own.
            // TODO: bubble this up (e.g. via a SharedFlow<RecoverableDeleteRequest>)
            // so the hosting Activity/Fragment can handle it with user consent flows.
            Log.w(TAG, "SecurityException deleting original $uri — needs user consent flow (API ${Build.VERSION.SDK_INT})", e)
        } catch (e: Exception) {
            Log.w(TAG, "Unexpected error deleting original $uri", e)
        }
    }
}
