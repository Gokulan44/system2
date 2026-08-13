package com.systemmonitor.features.intrusion.security

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoDecryptor @Inject constructor() {

    companion object {
        // Shared secret key matching Config.SECRET_KEY on the Windows Agent
        private const val SECRET_KEY = "system_monitor_laptop_remote_secret_2026"
    }

    /**
     * Decrypts the base64 AES-256-CBC encrypted photo payload and verifies its SHA-256 hash.
     * Returns a decoded Android Bitmap.
     */
    fun decryptAndVerify(encryptedB64: String, expectedHash: String?): Bitmap? {
        return try {
            val payloadBytes = Base64.decode(encryptedB64, Base64.DEFAULT)
            if (payloadBytes.size < 17) {
                return null
            }

            // 1. Extract 16-byte IV and ciphertext
            val iv = payloadBytes.sliceArray(0 until 16)
            val ciphertext = payloadBytes.sliceArray(16 until payloadBytes.size)

            // 2. Derive AES-256 key via SHA-256 of the shared secret key
            val keyBytes = MessageDigest.getInstance("SHA-256")
                .digest(SECRET_KEY.toByteArray(Charsets.UTF_8))

            // 3. Initialize AES/CBC/PKCS7Padding Cipher
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val ivSpec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            // 4. Decrypt
            val decryptedBytes = cipher.doFinal(ciphertext)

            // 5. Verify SHA-256 hash if expectedHash is provided
            if (!expectedHash.isNullOrEmpty()) {
                val calculatedHashBytes = MessageDigest.getInstance("SHA-256")
                    .digest(decryptedBytes)
                val calculatedHash = calculatedHashBytes.joinToString("") { "%02x".format(it) }
                if (calculatedHash != expectedHash) {
                    throw SecurityException("Intruder photo integrity validation failed! Expected: $expectedHash, Got: $calculatedHash")
                }
            }

            // 6. Decode into Android Bitmap
            BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
