package com.systemmonitor.applock.security

import com.systemmonitor.applock.settings.AppLockPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityPolicy @Inject constructor(
    private val preferences: AppLockPreferences
) {
    val maxFailedAttempts: Int
        get() = preferences.getSettings().maxFailedAttempts

    val lockoutDurationMs: Long = 30000L // 30 seconds lockout

    fun getDynamicLockoutDurationMs(): Long {
        return preferences.getSettings().sessionTimeoutSeconds * 1000L
    }
}
