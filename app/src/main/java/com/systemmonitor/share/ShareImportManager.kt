package com.systemmonitor.share

import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.importing.FileImportManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareImportManager @Inject constructor(
    private val fileImportManager: FileImportManager,
    private val validationManager: ShareValidationManager
) {
    suspend fun importSharedFiles(
        sharedFiles: List<SharedFile>,
        onProgress: (Int, Int) -> Unit
    ): List<Result<VaultFileEntity>> {
        val results = mutableListOf<Result<VaultFileEntity>>()
        sharedFiles.forEachIndexed { index, sharedFile ->
            onProgress(index + 1, sharedFiles.size)
            val validation = validationManager.validate(sharedFile)
            if (validation.isFailure) {
                results.add(Result.failure(validation.exceptionOrNull() ?: Exception("Validation failed")))
                return@forEachIndexed
            }

            val res = fileImportManager.importFile(
                uri = sharedFile.uri,
                name = sharedFile.name,
                mimeType = sharedFile.mimeType
            )
            results.add(res)
        }
        return results
    }
}
