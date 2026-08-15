package com.systemmonitor.vault.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultCryptoManager @Inject constructor() {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SystemMonitorVaultMasterKey"
        private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
        private const val IV_SIZE_BYTES = 12
        private const val TAG_SIZE_BITS = 128
    }

    private fun getOrGenerateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) return entry.secretKey
        }

        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        kg.init(spec)
        return kg.generateKey()
    }

    fun encryptFile(inputFile: File, outputFile: File) {
        val key = getOrGenerateKey()
        val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
        
        val iv = ByteArray(IV_SIZE_BYTES)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(TAG_SIZE_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                // 1. Write the IV
                fos.write(iv)
                
                // 2. Encrypt and write data
                val buffer = ByteArray(4096)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    val output = cipher.update(buffer, 0, read)
                    if (output != null) {
                        fos.write(output)
                    }
                }
                val finalOutput = cipher.doFinal()
                if (finalOutput != null) {
                    fos.write(finalOutput)
                }
            }
        }
    }

    fun decryptFile(inputFile: File, outputFile: File) {
        val key = getOrGenerateKey()
        val cipher = Cipher.getInstance(AES_GCM_NOPADDING)

        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                // 1. Read the IV
                val iv = ByteArray(IV_SIZE_BYTES)
                val bytesRead = fis.read(iv)
                if (bytesRead != IV_SIZE_BYTES) {
                    throw IllegalArgumentException("Invalid encrypted file: IV too short")
                }

                val spec = GCMParameterSpec(TAG_SIZE_BITS, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, spec)

                // 2. Decrypt and write data
                val buffer = ByteArray(4096)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    val output = cipher.update(buffer, 0, read)
                    if (output != null) {
                        fos.write(output)
                    }
                }
                val finalOutput = cipher.doFinal()
                if (finalOutput != null) {
                    fos.write(finalOutput)
                }
            }
        }
    }
}
