package com.systemmonitor.vault.importexport

import android.content.Context
import android.net.Uri
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportValidator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024 * 1024 // 2GB limit per file
    }

    fun validateUri(uri: Uri): ValidationResult {
        return try {
            val contentResolver = context.contentResolver
            
            // Check file size using AssetFileDescriptor length if available
            val sizeBytes = try {
                contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd -> afd.length } ?: -1L
            } catch (_: Exception) { -1L }

            if (sizeBytes > MAX_FILE_SIZE_BYTES) {
                return ValidationResult.Invalid("File size exceeds 2GB maximum limit")
            }

            // Verify input stream can be opened
            contentResolver.openInputStream(uri)?.use { 
                // Stream successfully opened
            } ?: return ValidationResult.Invalid("Unable to open URI input stream")

            // Check available internal storage space
            val internalDir = context.filesDir
            val stat = StatFs(internalDir.path)
            val freeBytes = stat.availableBytes
            if (freeBytes < 50 * 1024 * 1024L) { // Less than 50MB free
                return ValidationResult.Invalid("Insufficient storage space on device")
            }

            ValidationResult.Valid
        } catch (e: Exception) {
            ValidationResult.Invalid("Failed to validate import URI: ${e.message}")
        }
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
}
