package com.systemmonitor.applock.security

import com.systemmonitor.applock.data.database.AppLockSettingsDao
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityPolicy @Inject constructor(
    private val settingsDao: AppLockSettingsDao
) {
    val maxFailedAttempts: Int = 5
    val lockoutDurationMs: Long = 30000L // 30 seconds lockout

    suspend fun getDynamicLockoutDurationMs(): Long {
        val settings = settingsDao.getSettings().firstOrNull()
        return (settings?.sessionTimeout ?: 30) * 1000L
    }
}
