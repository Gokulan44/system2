package com.systemmonitor.vault.crypto

import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterKeyManager @Inject constructor(
    private val keyStoreManager: KeyStoreManager
) {
    fun getMasterKey(): SecretKey {
        return keyStoreManager.getOrGenerateMasterKey()
    }

    fun resetMasterKey() {
        keyStoreManager.purgeMasterKey()
    }
}
