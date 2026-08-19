package com.systemmonitor.vault.domain.usecase

import com.systemmonitor.vault.trash.VaultTrashManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteVaultFileUseCase @Inject constructor(
    private val trashManager: VaultTrashManager
) {
    suspend operator fun invoke(fileId: String) {
        trashManager.moveToTrash(fileId)
    }
}
