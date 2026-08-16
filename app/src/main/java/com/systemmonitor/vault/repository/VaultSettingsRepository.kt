package com.systemmonitor.vault.repository

import com.systemmonitor.vault.database.VaultSettingsDao
import com.systemmonitor.vault.database.VaultSettingsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultSettingsRepository @Inject constructor(
    private val settingsDao: VaultSettingsDao
) {
    suspend fun getSetting(key: String): String? {
        return settingsDao.getSetting(key)
    }

    suspend fun setSetting(key: String, value: String) {
        settingsDao.setSetting(VaultSettingsEntity(key, value))
    }

    suspend fun deleteSetting(key: String) {
        settingsDao.deleteSetting(key)
    }
}
