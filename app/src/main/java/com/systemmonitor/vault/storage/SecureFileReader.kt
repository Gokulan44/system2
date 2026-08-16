package com.systemmonitor.vault.storage

import com.systemmonitor.vault.crypto.VaultCryptoManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureFileReader @Inject constructor(
    private val cryptoManager: VaultCryptoManager
) {
    fun readDecryptedFile(encryptedFile: File, outputFile: File) {
        cryptoManager.decryptFile(encryptedFile, outputFile)
    }
}
