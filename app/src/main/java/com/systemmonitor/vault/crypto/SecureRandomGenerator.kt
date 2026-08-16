package com.systemmonitor.vault.crypto

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureRandomGenerator @Inject constructor() {
    private val secureRandom = SecureRandom()

    fun generateIv(): ByteArray {
        val iv = ByteArray(CryptoConstants.IV_SIZE_BYTES)
        secureRandom.nextBytes(iv)
        return iv
    }

    fun generateRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes
    }
}
