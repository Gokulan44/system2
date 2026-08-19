package com.systemmonitor.vault.authentication

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultPinManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockoutManager: LockoutManager
) {
    private val prefs = context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE)

    fun isPinSetup(): Boolean {
        return prefs.getString("vault_pin_hash", null) != null
    }

    fun setupPin(pin: String): Boolean {
        if (pin.length < 4) return false
        val hash = hashPin(pin)
        prefs.edit().putString("vault_pin_hash", hash).apply()
        lockoutManager.resetAttempts()
        return true
    }

    fun authenticate(pin: String): AuthenticationResult {
        if (lockoutManager.isLockedOut()) {
            return AuthenticationResult.LockedOut(lockoutManager.getRemainingCooldownMs())
        }

        val savedHash = prefs.getString("vault_pin_hash", null)
            ?: return AuthenticationResult.Error("No PIN configured")

        val hash = hashPin(pin)
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

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
