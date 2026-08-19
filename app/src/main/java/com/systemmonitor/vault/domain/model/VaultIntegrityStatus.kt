package com.systemmonitor.vault.domain.model

sealed class VaultIntegrityStatus {
    data class Valid(val message: String) : VaultIntegrityStatus()
    data class Corrupted(val reason: String) : VaultIntegrityStatus()
}
