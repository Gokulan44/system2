package com.systemmonitor.vault.importexport

import android.content.Context
import android.net.Uri
import com.systemmonitor.vault.database.VaultFileDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportValidator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDao: VaultFileDao
) {
    suspend fun validateExport(fileId: String, outputUri: Uri): ExportValidationResult {
        val entity = fileDao.getFileById(fileId)
            ?: return ExportValidationResult.Invalid("Vault file not found in database")

        val localEncryptedFile = File(entity.localPath)
        if (!localEncryptedFile.exists()) {
            return ExportValidationResult.Invalid("Encrypted file missing from disk")
        }

        try {
            context.contentResolver.openOutputStream(outputUri)?.close()
        } catch (e: Exception) {
            return ExportValidationResult.Invalid("Target destination URI is not writable")
        }

        return ExportValidationResult.Valid(entity)
    }

    sealed class ExportValidationResult {
        data class Valid(val fileEntity: com.systemmonitor.vault.database.VaultFileEntity) : ExportValidationResult()
        data class Invalid(val reason: String) : ExportValidationResult()
    }
}
