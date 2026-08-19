package com.systemmonitor.vault.domain.usecase

import android.net.Uri
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.importing.FileImportManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportMultipleFilesUseCase @Inject constructor(
    private val fileImportManager: FileImportManager
) {
    suspend operator fun invoke(
        uris: List<Uri>,
        parentId: String?,
        allowDuplicates: Boolean = false
    ): List<Result<VaultFileEntity>> {
        return fileImportManager.importMultipleFiles(uris, parentId, allowDuplicates)
    }
}
