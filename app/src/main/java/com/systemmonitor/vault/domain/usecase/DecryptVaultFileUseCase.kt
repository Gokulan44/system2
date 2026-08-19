package com.systemmonitor.vault.domain.usecase

import com.systemmonitor.vault.storage.VaultStorageManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DecryptVaultFileUseCase @Inject constructor(
    private val storageManager: VaultStorageManager
) {
    suspend operator fun invoke(fileId: String): File? {
        return storageManager.createTempDecryptedFile(fileId)
    }

    fun cleanupTempFiles() {
        storageManager.cleanupTempFiles()
    }
}
