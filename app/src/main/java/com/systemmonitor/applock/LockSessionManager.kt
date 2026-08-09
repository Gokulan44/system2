package com.systemmonitor.applock

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockSessionManager @Inject constructor() {
    private val unlockedApps = mutableSetOf<String>()

    fun grantTemporaryUnlock(packageName: String) {
        unlockedApps.add(packageName)
    }

    fun isTemporarilyUnlocked(packageName: String): Boolean {
        return unlockedApps.contains(packageName)
    }

    fun clearSession() {
        unlockedApps.clear()
    }
}
