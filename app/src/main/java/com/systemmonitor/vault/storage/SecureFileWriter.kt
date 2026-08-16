package com.systemmonitor.vault.storage

import com.systemmonitor.vault.crypto.VaultCryptoManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureFileWriter @Inject constructor(
    private val cryptoManager: VaultCryptoManager
) {
    fun writeEncryptedFile(sourceFile: File, destinationFile: File) {
        cryptoManager.encryptFile(sourceFile, destinationFile)
    }
}
