package com.systemmonitor.vault.domain.usecase

import android.net.Uri
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.importing.FileImportManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportVaultFileUseCase @Inject constructor(
    private val importManager: FileImportManager
) {
    suspend operator fun invoke(
        uri: Uri,
        parentId: String?,
        allowDuplicates: Boolean = false,
        deleteOriginalAfterImport: Boolean = true
    ): Result<VaultFileEntity> {
        return importManager.importFile(
            uri = uri,
            parentId = parentId,
            allowDuplicates = allowDuplicates,
            deleteOriginalAfterImport = deleteOriginalAfterImport
        )
    }

    suspend fun importMultiple(
        uris: List<Uri>,
        parentId: String?,
        allowDuplicates: Boolean = false,
        deleteOriginalAfterImport: Boolean = true
    ): List<Result<VaultFileEntity>> {
        return importManager.importMultipleFiles(
            uris = uris,
            parentId = parentId,
            allowDuplicates = allowDuplicates,
            deleteOriginalAfterImport = deleteOriginalAfterImport
        )
    }
}