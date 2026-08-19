package com.systemmonitor.share

import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.domain.usecase.ImportSharedFileUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareImportManager @Inject constructor(
    private val importSharedFileUseCase: ImportSharedFileUseCase
) {
    suspend fun importSharedFiles(
        sharedFiles: List<SharedFile>,
        onProgress: (Int, Int) -> Unit
    ): List<Result<VaultFileEntity>> {
        val results = mutableListOf<Result<VaultFileEntity>>()
        sharedFiles.forEachIndexed { index, sharedFile ->
            onProgress(index + 1, sharedFiles.size)
            val res = importSharedFileUseCase(sharedFile)
            results.add(res)
        }
        return results
    }
}
