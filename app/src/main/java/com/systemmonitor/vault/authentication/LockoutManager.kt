package com.systemmonitor.vault.authentication

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class FailedAttemptResult {
    data class AttemptsRemaining(val remaining: Int) : FailedAttemptResult()
    data class LockedOut(val cooldownMs: Long) : FailedAttemptResult()
    object WipeTriggered : FailedAttemptResult()
}

@Singleton
class LockoutManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("vault_lockout_prefs", Context.MODE_PRIVATE)

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5          // triggers a temporary cooldown
        const val LOCKOUT_DURATION_MS = 30_000L    // 30 seconds cooldown
        const val WIPE_THRESHOLD_ATTEMPTS = 10     // triggers a full panic wipe

        private const val KEY_CYCLE_ATTEMPTS = "failed_attempts"        // resets each cooldown cycle
        private const val KEY_TOTAL_ATTEMPTS = "total_failed_attempts"  // never resets except on success/wipe
        private const val KEY_LOCKOUT_END = "lockout_end_time"
    }

    fun isLockedOut(): Boolean {
        val lockoutEndTime = prefs.getLong(KEY_LOCKOUT_END, 0L)
        val now = System.currentTimeMillis()
        if (now < lockoutEndTime) {
            return true
        } else if (lockoutEndTime != 0L) {
            // Cooldown expired: clear the per-cycle counter and the lockout
            // timestamp so the user can try again, but deliberately do NOT
            // touch the total-attempts counter — that only resets on a
            // successful login or an actual wipe, so cycling through
            // cooldowns can't be used to dodge the wipe threshold.
            prefs.edit()
                .remove(KEY_CYCLE_ATTEMPTS)
                .remove(KEY_LOCKOUT_END)
                .apply()
        }
        return false
    }

    fun getRemainingCooldownMs(): Long {
        val lockoutEndTime = prefs.getLong(KEY_LOCKOUT_END, 0L)
        val now = System.currentTimeMillis()
        return if (lockoutEndTime > now) lockoutEndTime - now else 0L
    }

    /**
     * Records a failed authentication attempt and returns what should happen
     * next: keep going with N attempts remaining, enter a cooldown, or —
     * if the total (cross-cooldown) failure count has hit the wipe
     * threshold — trigger a full panic wipe.
     *
     * Callers MUST check for [FailedAttemptResult.WipeTriggered] and, if
     * received, invoke the vault wipe (see VaultWipeManager) before
     * reporting the result to the user.
     */
    fun recordFailedAttempt(): FailedAttemptResult {
        val cycleCount = prefs.getInt(KEY_CYCLE_ATTEMPTS, 0) + 1
        val totalCount = prefs.getInt(KEY_TOTAL_ATTEMPTS, 0) + 1
        prefs.edit()
            .putInt(KEY_CYCLE_ATTEMPTS, cycleCount)
            .putInt(KEY_TOTAL_ATTEMPTS, totalCount)
            .apply()

        if (totalCount >= WIPE_THRESHOLD_ATTEMPTS) {
            return FailedAttemptResult.WipeTriggered
        }

        if (cycleCount >= MAX_FAILED_ATTEMPTS) {
            val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            prefs.edit().putLong(KEY_LOCKOUT_END, lockoutUntil).apply()
            return FailedAttemptResult.LockedOut(LOCKOUT_DURATION_MS)
        }

        return FailedAttemptResult.AttemptsRemaining(MAX_FAILED_ATTEMPTS - cycleCount)
    }

    /** Call on successful authentication, and after a completed wipe. */
    fun resetAttempts() {
        prefs.edit()
            .remove(KEY_CYCLE_ATTEMPTS)
            .remove(KEY_TOTAL_ATTEMPTS)
            .remove(KEY_LOCKOUT_END)
            .apply()
    }
}