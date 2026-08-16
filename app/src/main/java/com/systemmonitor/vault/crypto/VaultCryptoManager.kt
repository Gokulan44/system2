package com.systemmonitor.vault.crypto

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultCryptoManager @Inject constructor(
    private val encryptionEngine: EncryptionEngine,
    private val decryptionEngine: DecryptionEngine,
    private val masterKeyManager: MasterKeyManager
) {
    fun encryptFile(inputFile: File, outputFile: File) {
        encryptionEngine.encryptFile(inputFile, outputFile)
    }

    fun decryptFile(inputFile: File, outputFile: File) {
        decryptionEngine.decryptFile(inputFile, outputFile)
    }

    fun resetMasterKey() {
        masterKeyManager.resetMasterKey()
    }
}
