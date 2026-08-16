package com.systemmonitor.vault.authentication

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockoutManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("vault_lockout_prefs", Context.MODE_PRIVATE)

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds cooldown
    }

    fun isLockedOut(): Boolean {
        val lockoutEndTime = prefs.getLong("lockout_end_time", 0L)
        val now = System.currentTimeMillis()
        if (now < lockoutEndTime) {
            return true
        } else if (lockoutEndTime != 0L) {
            resetAttempts()
        }
        return false
    }

    fun getRemainingCooldownMs(): Long {
        val lockoutEndTime = prefs.getLong("lockout_end_time", 0L)
        val now = System.currentTimeMillis()
        return if (lockoutEndTime > now) lockoutEndTime - now else 0L
    }

    fun recordFailedAttempt(): Int {
        val current = prefs.getInt("failed_attempts", 0) + 1
        prefs.edit().putInt("failed_attempts", current).apply()

        if (current >= MAX_FAILED_ATTEMPTS) {
            val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            prefs.edit().putLong("lockout_end_time", lockoutUntil).apply()
        }
        return MAX_FAILED_ATTEMPTS - current
    }

    fun resetAttempts() {
        prefs.edit()
            .remove("failed_attempts")
            .remove("lockout_end_time")
            .apply()
    }
}
