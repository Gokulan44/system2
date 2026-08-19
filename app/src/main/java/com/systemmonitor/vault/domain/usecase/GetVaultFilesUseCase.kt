package com.systemmonitor.vault.domain.usecase

import com.systemmonitor.vault.model.VaultFile
import com.systemmonitor.vault.repository.VaultFileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetVaultFilesUseCase @Inject constructor(
    private val fileRepository: VaultFileRepository
) {
    operator fun invoke(parentId: String?): Flow<List<VaultFile>> {
        return fileRepository.getFilesInFolder(parentId)
    }

    fun getAllFiles(): Flow<List<VaultFile>> {
        return fileRepository.getAllFiles()
    }
}
