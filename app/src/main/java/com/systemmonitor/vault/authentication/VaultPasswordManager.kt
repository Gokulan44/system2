package com.systemmonitor.vault.authentication

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultPasswordManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockoutManager: LockoutManager
) {
    private val prefs = context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE)

    fun isPasswordSetup(): Boolean {
        return prefs.getString("vault_password_hash", null) != null
    }

    fun setupPassword(password: String): Boolean {
        if (password.length < 6) return false
        val hash = hashPassword(password)
        prefs.edit().putString("vault_password_hash", hash).apply()
        lockoutManager.resetAttempts()
        return true
    }

    fun authenticate(password: String): AuthenticationResult {
        if (lockoutManager.isLockedOut()) {
            return AuthenticationResult.LockedOut(lockoutManager.getRemainingCooldownMs())
        }

        val savedHash = prefs.getString("vault_password_hash", null)
            ?: return AuthenticationResult.Error("No password configured")

        val hash = hashPassword(password)
        return if (hash == savedHash) {
            lockoutManager.resetAttempts()
            AuthenticationResult.Success
        } else {
            val remaining = lockoutManager.recordFailedAttempt()
            if (remaining <= 0) {
                AuthenticationResult.LockedOut(lockoutManager.getRemainingCooldownMs())
            } else {
                AuthenticationResult.InvalidCredentials(remaining)
            }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
