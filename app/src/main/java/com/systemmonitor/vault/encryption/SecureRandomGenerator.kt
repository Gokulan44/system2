package com.systemmonitor.vault.encryption

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureRandomGenerator @Inject constructor() {
    private val secureRandom = SecureRandom()

    fun generateIv(size: Int = CryptoConstants.IV_SIZE_BYTES): ByteArray {
        val iv = ByteArray(size)
        secureRandom.nextBytes(iv)
        return iv
    }
}
