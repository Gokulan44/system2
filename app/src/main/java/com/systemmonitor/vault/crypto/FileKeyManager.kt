package com.systemmonitor.vault.crypto

import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileKeyManager @Inject constructor(
    private val masterKeyManager: MasterKeyManager,
    private val secureRandomGenerator: SecureRandomGenerator
) {
    fun generateFileKey(): SecretKey {
        val kg = KeyGenerator.getInstance("AES")
        kg.init(CryptoConstants.KEY_SIZE_BITS)
        return kg.generateKey()
    }

    fun wrapFileKey(fileKey: SecretKey): ByteArray {
        // Master key encrypts file key for per-file key isolation
        val masterKey = masterKeyManager.getMasterKey()
        val cipher = javax.crypto.Cipher.getInstance(CryptoConstants.AES_GCM_NOPADDING)
        val iv = secureRandomGenerator.generateIv()
        val spec = javax.crypto.spec.GCMParameterSpec(CryptoConstants.TAG_SIZE_BITS, iv)
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, masterKey, spec)

        val encryptedKeyBytes = cipher.doFinal(fileKey.encoded)
        return iv + encryptedKeyBytes
    }

    fun unwrapFileKey(wrappedData: ByteArray): SecretKey {
        val masterKey = masterKeyManager.getMasterKey()
        val iv = wrappedData.copyOfRange(0, CryptoConstants.IV_SIZE_BYTES)
        val encryptedKey = wrappedData.copyOfRange(CryptoConstants.IV_SIZE_BYTES, wrappedData.size)

        val cipher = javax.crypto.Cipher.getInstance(CryptoConstants.AES_GCM_NOPADDING)
        val spec = javax.crypto.spec.GCMParameterSpec(CryptoConstants.TAG_SIZE_BITS, iv)
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, masterKey, spec)

        val rawKey = cipher.doFinal(encryptedKey)
        return SecretKeySpec(rawKey, "AES")
    }
}
