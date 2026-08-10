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
        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
        private const val KEY_LOCKOUT_START_TIME = "key_lockout_start_time"
        private const val LOCKOUT_DURATION_MS = 30000L
    }

    fun isPatternSet(): Boolean = prefs.contains(KEY_PATTERN_HASH)

    fun createPattern(patternPoints: List<Int>): Boolean {
        if (patternPoints.size < 4) return false
        val patternStr = patternPoints.joinToString("-")
        val hash = cryptoManager.hashPin(patternStr)
        prefs.edit()
            .putString(KEY_PATTERN_HASH, hash)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_START_TIME, 0L)
            .apply()
        return true
    }

    fun confirmPattern(initial: List<Int>, confirmation: List<Int>): Boolean {
        return initial == confirmation
    }

    fun getLockoutTimeRemaining(): Long {
        val lockoutStart = prefs.getLong(KEY_LOCKOUT_START_TIME, 0L)
        if (lockoutStart == 0L) return 0L
        val elapsed = System.currentTimeMillis() - lockoutStart
        return if (elapsed < LOCKOUT_DURATION_MS) {
            LOCKOUT_DURATION_MS - elapsed
        } else {
            prefs.edit()
                .putLong(KEY_LOCKOUT_START_TIME, 0L)
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .apply()
            0L
        }
    }

    fun verifyPattern(enteredPoints: List<Int>): AuthenticationResult {
        val remainingLockout = getLockoutTimeRemaining()
        if (remainingLockout > 0) {
            val seconds = (remainingLockout / 1000).toInt().coerceAtLeast(1)
            return AuthenticationResult.Lockout(seconds)
        }

        val storedHash = prefs.getString(KEY_PATTERN_HASH, null) ?: return AuthenticationResult.Failed(0)
        val enteredStr = enteredPoints.joinToString("-")
        val enteredHash = cryptoManager.hashPin(enteredStr)

        return if (storedHash == enteredHash) {
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_START_TIME, 0L)
                .apply()
            AuthenticationResult.Success
        } else {
            val failedCount = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failedCount)
            if (failedCount >= 5) {
                editor.putLong(KEY_LOCKOUT_START_TIME, System.currentTimeMillis())
            }
            editor.apply()

            val remaining = (5 - failedCount).coerceAtLeast(0)
            if (failedCount >= 5) {
                AuthenticationResult.Lockout(30)
            } else {
                AuthenticationResult.Failed(remaining, isLockedOut = false)
            }
        }
    }
}
