package com.systemmonitor.vault.authentication

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultPasswordManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockoutManager: LockoutManager,
    private val wipeManager: VaultWipeManager
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

    suspend fun authenticate(password: String): AuthenticationResult {
        if (lockoutManager.isLockedOut()) {
            return AuthenticationResult.LockedOut(lockoutManager.getRemainingCooldownMs())
        }

        val savedHash = prefs.getString("vault_password_hash", null)
            ?: return AuthenticationResult.Error("No password configured")

        val hash = hashPassword(password)
        if (hash == savedHash) {
            lockoutManager.resetAttempts()
            return AuthenticationResult.Success
        }

        return when (val failure = lockoutManager.recordFailedAttempt()) {
            is FailedAttemptResult.WipeTriggered -> {
                wipeManager.wipeVault()
                AuthenticationResult.VaultWiped(
                    "Too many failed attempts. The vault has been permanently wiped for security."
                )
            }
            is FailedAttemptResult.LockedOut ->
                AuthenticationResult.LockedOut(failure.cooldownMs)
            is FailedAttemptResult.AttemptsRemaining ->
                AuthenticationResult.InvalidCredentials(failure.remaining)
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
