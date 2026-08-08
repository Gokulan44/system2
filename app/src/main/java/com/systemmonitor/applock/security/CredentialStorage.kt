package com.systemmonitor.applock.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)

    fun saveCredential(credential: String, type: String) {
        val hash = hash(credential)
        prefs.edit()
            .putString("lock_hash", hash)
            .putString("lock_type", type)
            .apply()
    }

    fun verifyCredential(input: String): Boolean {
        val savedHash = prefs.getString("lock_hash", null) ?: return true
        return hash(input) == savedHash
    }

    fun getLockType(): String {
        return prefs.getString("lock_type", "PIN") ?: "PIN"
    }

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
