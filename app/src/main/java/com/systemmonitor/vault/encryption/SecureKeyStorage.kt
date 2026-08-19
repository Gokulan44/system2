package com.systemmonitor.vault.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureKeyStorage @Inject constructor() {
    fun getOrGenerateMasterKey(alias: String = CryptoConstants.MASTER_KEY_ALIAS): SecretKey {
        val keyStore = KeyStore.getInstance(CryptoConstants.ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(alias)) {
            val entry = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
            if (entry != null) return entry.secretKey
        }

        val kg = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            CryptoConstants.ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(CryptoConstants.KEY_SIZE_BITS)
            .build()

        kg.init(spec)
        return kg.generateKey()
    }

    fun purgeMasterKey(alias: String = CryptoConstants.MASTER_KEY_ALIAS) {
        val keyStore = KeyStore.getInstance(CryptoConstants.ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }
}
