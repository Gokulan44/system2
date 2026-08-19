package com.systemmonitor.vault.storage

import com.systemmonitor.vault.encryption.VaultEncryptionManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureFileWriter @Inject constructor(
    private val cryptoManager: VaultEncryptionManager
) {
    fun writeEncryptedFile(sourceFile: File, destinationFile: File) {
        cryptoManager.encryptFile(sourceFile, destinationFile)
    }
}
