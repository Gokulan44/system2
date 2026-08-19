package com.systemmonitor.vault.storage

import com.systemmonitor.vault.encryption.VaultEncryptionManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureFileReader @Inject constructor(
    private val cryptoManager: VaultEncryptionManager
) {
    fun readDecryptedFile(encryptedFile: File, outputFile: File) {
        cryptoManager.decryptFile(encryptedFile, outputFile)
    }
}
