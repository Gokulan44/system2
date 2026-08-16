package com.systemmonitor.vault.recovery

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("vault_recovery_prefs", Context.MODE_PRIVATE)

    fun generateRecoveryKey(): String {
        val rawKey = UUID.randomUUID().toString().replace("-", "").take(16).uppercase()
        val hash = hashKey(rawKey)
        prefs.edit().putString("recovery_key_hash", hash).apply()
        return rawKey.chunked(4).joinToString("-")
    }

    fun verifyRecoveryKey(userKey: String): Boolean {
        val cleanKey = userKey.replace("-", "").replace(" ", "").uppercase()
        val savedHash = prefs.getString("recovery_key_hash", null) ?: return false
        return hashKey(cleanKey) == savedHash
    }

    private fun hashKey(key: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}

@Singleton
class VaultRecoveryManager @Inject constructor(
    val recoveryKeyManager: RecoveryKeyManager
)
