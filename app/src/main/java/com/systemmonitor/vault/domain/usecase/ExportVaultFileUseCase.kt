package com.systemmonitor.vault.domain.usecase

import android.net.Uri
import com.systemmonitor.vault.importexport.VaultExportManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportVaultFileUseCase @Inject constructor(
    private val exportManager: VaultExportManager
) {
    suspend operator fun invoke(fileId: String, outputUri: Uri): Result<Boolean> {
        return exportManager.exportSingleFile(fileId, outputUri)
    }
}
