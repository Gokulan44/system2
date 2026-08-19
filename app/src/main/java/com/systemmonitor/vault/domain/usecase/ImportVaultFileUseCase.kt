package com.systemmonitor.vault.domain.usecase

import android.net.Uri
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.importexport.VaultImportManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportVaultFileUseCase @Inject constructor(
    private val importManager: VaultImportManager
) {
    suspend operator fun invoke(
        uri: Uri,
        parentId: String?,
        allowDuplicates: Boolean = false
    ): Result<VaultFileEntity> {
        return importManager.importSingleFile(uri, parentId, allowDuplicates)
    }

    suspend fun importMultiple(
        uris: List<Uri>,
        parentId: String?,
        allowDuplicates: Boolean = false
    ): List<Result<VaultFileEntity>> {
        return importManager.importMultipleFiles(uris, parentId, allowDuplicates)
    }
}
