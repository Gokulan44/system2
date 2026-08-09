package com.systemmonitor.applock.authentication

import android.content.Context
import android.content.SharedPreferences
import com.systemmonitor.applock.security.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("applock_pattern_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PATTERN_HASH = "key_pattern_hash"
    }

    fun isPatternSet(): Boolean = prefs.contains(KEY_PATTERN_HASH)

    fun createPattern(patternPoints: List<Int>): Boolean {
        if (patternPoints.size < 4) return false
        val patternStr = patternPoints.joinToString("-")
        val hash = cryptoManager.hashPin(patternStr)
        prefs.edit().putString(KEY_PATTERN_HASH, hash).apply()
        return true
    }

    fun confirmPattern(initial: List<Int>, confirmation: List<Int>): Boolean {
        return initial == confirmation
    }

    fun verifyPattern(enteredPoints: List<Int>): AuthenticationResult {
        val storedHash = prefs.getString(KEY_PATTERN_HASH, null) ?: return AuthenticationResult.Failed(0)
        val enteredStr = enteredPoints.joinToString("-")
        val enteredHash = cryptoManager.hashPin(enteredStr)

        return if (storedHash == enteredHash) {
            AuthenticationResult.Success
        } else {
            AuthenticationResult.Failed(3)
        }
    }
}
