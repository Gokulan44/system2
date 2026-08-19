package com.systemmonitor.vault.authentication

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultPatternManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE)

    fun isPatternSetup(): Boolean {
        return prefs.getString("vault_pattern_hash", null) != null
    }

    fun setupPattern(pattern: String): Boolean {
        if (pattern.length < 4) return false
        prefs.edit().putString("vault_pattern_hash", pattern).apply()
        return true
    }

    fun authenticate(pattern: String): AuthenticationResult {
        val savedPattern = prefs.getString("vault_pattern_hash", null)
            ?: return AuthenticationResult.Error("No pattern configured")
        return if (pattern == savedPattern) {
            AuthenticationResult.Success
        } else {
            AuthenticationResult.InvalidCredentials(3)
        }
    }
}
