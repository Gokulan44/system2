package com.systemmonitor.vault.encryption

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileDecryptor @Inject constructor(
    private val aesGcmCipher: AesGcmCipher,
    private val keyManager: VaultKeyManager
) {
    fun decryptFile(inputFile: File, outputFile: File, key: SecretKey = keyManager.getMasterKey()) {
        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                aesGcmCipher.decryptStream(fis, fos, key)
            }
        }
    }
}
