package com.systemmonitor.vault.crypto

import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AesGcmCipher @Inject constructor(
    private val secureRandomGenerator: SecureRandomGenerator
) {
    fun encryptStream(
        inputStream: InputStream,
        outputStream: OutputStream,
        key: SecretKey
    ) {
        val cipher = Cipher.getInstance(CryptoConstants.AES_GCM_NOPADDING)
        val iv = secureRandomGenerator.generateIv()
        val spec = GCMParameterSpec(CryptoConstants.TAG_SIZE_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        // Write IV to start of file
        outputStream.write(iv)

        val buffer = ByteArray(CryptoConstants.BUFFER_SIZE_BYTES)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            val output = cipher.update(buffer, 0, read)
            if (output != null && output.isNotEmpty()) {
                outputStream.write(output)
            }
        }
        val finalOutput = cipher.doFinal()
        if (finalOutput != null && finalOutput.isNotEmpty()) {
            outputStream.write(finalOutput)
        }
    }

    fun decryptStream(
        inputStream: InputStream,
        outputStream: OutputStream,
        key: SecretKey
    ) {
        val cipher = Cipher.getInstance(CryptoConstants.AES_GCM_NOPADDING)
        val iv = ByteArray(CryptoConstants.IV_SIZE_BYTES)
        val bytesRead = inputStream.read(iv)
        if (bytesRead != CryptoConstants.IV_SIZE_BYTES) {
            throw IllegalArgumentException("Corrupt encrypted file: IV header truncated")
        }

        val spec = GCMParameterSpec(CryptoConstants.TAG_SIZE_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val buffer = ByteArray(CryptoConstants.BUFFER_SIZE_BYTES)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            val output = cipher.update(buffer, 0, read)
            if (output != null && output.isNotEmpty()) {
                outputStream.write(output)
            }
        }
        val finalOutput = cipher.doFinal()
        if (finalOutput != null && finalOutput.isNotEmpty()) {
            outputStream.write(finalOutput)
        }
    }
}
