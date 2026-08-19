package com.systemmonitor.vault.encryption

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultEncryptionManager @Inject constructor(
    private val fileEncryptor: FileEncryptor,
    private val fileDecryptor: FileDecryptor,
    private val keyManager: VaultKeyManager
) {
    fun encryptFile(inputFile: File, outputFile: File) {
        fileEncryptor.encryptFile(inputFile, outputFile)
    }

    fun decryptFile(inputFile: File, outputFile: File) {
        fileDecryptor.decryptFile(inputFile, outputFile)
    }

    fun resetMasterKey() {
        keyManager.resetMasterKey()
    }
}
