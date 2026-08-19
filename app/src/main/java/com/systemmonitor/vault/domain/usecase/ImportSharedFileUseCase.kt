package com.systemmonitor.vault.domain.usecase

import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.importing.FileImportManager
import com.systemmonitor.share.SharedFile
import com.systemmonitor.share.ShareValidationManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportSharedFileUseCase @Inject constructor(
    private val fileImportManager: FileImportManager,
    private val validationManager: ShareValidationManager
) {
    suspend operator fun invoke(sharedFile: SharedFile): Result<VaultFileEntity> {
        val validationResult = validationManager.validate(sharedFile)
        if (validationResult.isFailure) {
            return Result.failure(validationResult.exceptionOrNull() ?: Exception("Validation failed"))
        }
        return fileImportManager.importFile(
            uri = sharedFile.uri,
            name = sharedFile.name,
            mimeType = sharedFile.mimeType
        )
    }
}
