package com.systemmonitor.applock.authentication

import android.content.Context
import android.content.SharedPreferences
import com.systemmonitor.applock.security.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("applock_password_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PASSWORD_HASH = "key_password_hash"
    }

    fun isPasswordSet(): Boolean = prefs.contains(KEY_PASSWORD_HASH)

    fun createPassword(password: String): Boolean {
        if (password.length < 4) return false
        val hash = cryptoManager.hashPin(password)
        prefs.edit().putString(KEY_PASSWORD_HASH, hash).apply()
        return true
    }

    fun verifyPassword(enteredPassword: String): AuthenticationResult {
        val storedHash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return AuthenticationResult.Failed(0)
        val enteredHash = cryptoManager.hashPin(enteredPassword)
        return if (storedHash == enteredHash) {
            AuthenticationResult.Success
        } else {
            AuthenticationResult.Failed(3)
        }
    }

    fun changePassword(oldPass: String, newPass: String): Boolean {
        if (verifyPassword(oldPass) is AuthenticationResult.Success) {
            return createPassword(newPass)
        }
        return false
    }
}
