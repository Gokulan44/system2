package com.systemmonitor.vault.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepository @Inject constructor(
    val files: VaultFileRepository,
    val folders: VaultFolderRepository,
    val settings: VaultSettingsRepository,
    val audits: VaultAuditRepository
)
