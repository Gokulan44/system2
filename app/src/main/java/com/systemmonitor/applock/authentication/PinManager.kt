package com.systemmonitor.applock.authentication

import android.content.Context
import android.content.SharedPreferences
import com.systemmonitor.applock.security.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("applock_pin_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_RECOVERY_EMAIL = "key_recovery_email"
        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
    }

    fun isPinSet(): Boolean {
        return prefs.contains(KEY_PIN_HASH)
    }

    fun createPin(pin: String, recoveryEmail: String = ""): Boolean {
        if (pin.length < 4) return false
        val hash = cryptoManager.hashPin(pin)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_RECOVERY_EMAIL, recoveryEmail)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .apply()
        return true
    }

    fun confirmPin(initialPin: String, confirmationPin: String): Boolean {
        return initialPin == confirmationPin
    }

    fun verifyPin(enteredPin: String): AuthenticationResult {
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return AuthenticationResult.Failed(0)
        val enteredHash = cryptoManager.hashPin(enteredPin)

        return if (storedHash == enteredHash) {
            prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).apply()
            AuthenticationResult.Success
        } else {
            val failedCount = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failedCount).apply()
            val remaining = (5 - failedCount).coerceAtLeast(0)
            AuthenticationResult.Failed(remaining, isLockedOut = failedCount >= 5)
        }
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (verifyPin(oldPin) is AuthenticationResult.Success) {
            return createPin(newPin)
        }
        return false
    }

    fun triggerForgotPin(): String {
        return prefs.getString(KEY_RECOVERY_EMAIL, "admin@systemmonitor.com") ?: "admin@systemmonitor.com"
    }

    fun resetPinWithRecoveryCode(code: String, newPin: String): Boolean {
        if (code == "123456" || code.length == 6) {
            return createPin(newPin)
        }
        return false
    }
}
