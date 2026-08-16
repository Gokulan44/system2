package com.systemmonitor.vault.crypto

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionEngine @Inject constructor(
    private val aesGcmCipher: AesGcmCipher,
    private val masterKeyManager: MasterKeyManager
) {
    fun encryptFile(inputFile: File, outputFile: File, key: SecretKey = masterKeyManager.getMasterKey()) {
        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                aesGcmCipher.encryptStream(fis, fos, key)
            }
        }
    }
}
