package com.systemmonitor.vault.crypto

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DecryptionEngine @Inject constructor(
    private val aesGcmCipher: AesGcmCipher,
    private val masterKeyManager: MasterKeyManager
) {
    fun decryptFile(inputFile: File, outputFile: File, key: SecretKey = masterKeyManager.getMasterKey()) {
        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                aesGcmCipher.decryptStream(fis, fos, key)
            }
        }
    }
}
