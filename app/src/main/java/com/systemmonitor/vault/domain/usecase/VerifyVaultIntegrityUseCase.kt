package com.systemmonitor.vault.domain.usecase

import com.systemmonitor.vault.domain.model.VaultIntegrityStatus
import com.systemmonitor.vault.security.FileIntegrityManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerifyVaultIntegrityUseCase @Inject constructor(
    private val integrityManager: FileIntegrityManager
) {
    suspend operator fun invoke(fileId: String): VaultIntegrityStatus {
        return when (val result = integrityManager.verifyIntegrity(fileId)) {
            is FileIntegrityManager.IntegrityResult.Valid -> VaultIntegrityStatus.Valid(result.message)
            is FileIntegrityManager.IntegrityResult.Corrupted -> VaultIntegrityStatus.Corrupted(result.reason)
        }
    }
}
