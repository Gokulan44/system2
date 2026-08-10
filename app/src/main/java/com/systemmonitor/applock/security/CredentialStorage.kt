package com.systemmonitor.applock.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)

    fun hasCredential(): Boolean = prefs.contains(KEY_LOCK_HASH)

    fun saveCredential(credential: String, type: String) {
        val hash = hash(credential)
        prefs.edit()
            .putString(KEY_LOCK_HASH, hash)
            .putString("lock_type", type)
            .apply()
    }

    /**
     * FAIL-CLOSED: previously returned `true` when no hash existed, meaning any
     * PIN unlocked the app before a credential was ever set. Now returns false.
     */
    fun verifyCredential(input: String): Boolean {
        val savedHash = prefs.getString(KEY_LOCK_HASH, null) ?: return false
        return hash(input) == savedHash
    }

    fun getLockType(): String {
        return prefs.getString("lock_type", "PIN") ?: "PIN"
    }

    fun saveRecoveryCode(code: String) {
        prefs.edit().putString(KEY_RECOVERY_HASH, hash(code.trim())).apply()
    }

    fun verifyRecoveryCode(code: String): Boolean {
        val savedHash = prefs.getString(KEY_RECOVERY_HASH, null) ?: return false
        return hash(code.trim()) == savedHash
    }

    fun hasRecoveryCode(): Boolean = prefs.contains(KEY_RECOVERY_HASH)

    fun generateRecoveryCode(): String {
        val code = (SecureRandom().nextInt(900000) + 100000).toString()
        saveRecoveryCode(code)
        return code
    }

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val KEY_LOCK_HASH = "lock_hash"
        const val KEY_RECOVERY_HASH = "recovery_hash"
    }
}