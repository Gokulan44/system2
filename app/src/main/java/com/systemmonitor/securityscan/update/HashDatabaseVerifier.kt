package com.systemmonitor.securityscan.update

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HashDatabaseVerifier @Inject constructor() {
    fun verifyChecksum(content: String, expectedChecksum: String): Boolean {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(content.toByteArray(Charsets.UTF_8))
            val hexString = StringBuilder()
            for (b in hashBytes) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            hexString.toString().lowercase() == expectedChecksum.lowercase()
        } catch (e: Exception) {
            false
        }
    }
}
