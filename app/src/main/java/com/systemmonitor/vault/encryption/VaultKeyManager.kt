package com.systemmonitor.vault.encryption

import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultKeyManager @Inject constructor(
    private val keyStoreStorage: SecureKeyStorage
) {
    fun getMasterKey(): SecretKey {
        return keyStoreStorage.getOrGenerateMasterKey()
    }

    fun resetMasterKey() {
        keyStoreStorage.purgeMasterKey()
    }
}
