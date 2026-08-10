package com.systemmonitor.applock

import com.systemmonitor.applock.model.LockTiming
import com.systemmonitor.applock.settings.AppLockPreferences
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockSessionManager @Inject constructor(
    private val preferences: AppLockPreferences
) {
    private val unlockedApps = ConcurrentHashMap<String, Long>()

    fun grantTemporaryUnlock(packageName: String) {
        unlockedApps[packageName] = System.currentTimeMillis()
    }

    fun isTemporarilyUnlocked(packageName: String): Boolean {
        val lastUnlockTime = unlockedApps[packageName] ?: return false
        val settings = preferences.getSettings()

        val elapsed = System.currentTimeMillis() - lastUnlockTime
        return when (settings.lockTiming) {
            LockTiming.IMMEDIATELY -> elapsed < 1000L // 1 second grace period to prevent double lock on transition
            LockTiming.AFTER_30_SECONDS -> elapsed < 30000L
            LockTiming.AFTER_1_MINUTE -> elapsed < 60000L
            LockTiming.AFTER_SCREEN_OFF -> true // Persists until explicitly cleared
        }
    }

    fun clearSession() {
        unlockedApps.clear()
    }

    fun getLockOnScreenOff(): Boolean {
        val settings = preferences.getSettings()
        return settings.lockOnScreenOff || settings.lockTiming == LockTiming.AFTER_SCREEN_OFF
    }
}

