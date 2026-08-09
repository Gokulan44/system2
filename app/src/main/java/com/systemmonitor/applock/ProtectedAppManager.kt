package com.systemmonitor.applock

import com.systemmonitor.applock.data.repository.AppLockDataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtectedAppManager @Inject constructor(
    private val repository: AppLockDataRepository
) {
    val lockedApps = repository.getLockedApps()

    suspend fun isProtected(packageName: String): Boolean {
        return repository.isAppLocked(packageName)
    }
}
