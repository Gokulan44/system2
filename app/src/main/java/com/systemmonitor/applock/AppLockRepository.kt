package com.systemmonitor.applock

import com.systemmonitor.applock.database.LockedAppEntity
import com.systemmonitor.applock.manager.AppLockManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockRepository @Inject constructor(
    private val appLockManager: AppLockManager
) {
    fun getLockedApps(): Flow<List<LockedAppEntity>> = appLockManager.getLockedApps()

    suspend fun setAppLocked(packageName: String, appName: String, isLocked: Boolean) {
        appLockManager.setAppLocked(packageName, appName, isLocked)
    }

    suspend fun isAppProtected(packageName: String): Boolean {
        return appLockManager.isAppProtected(packageName)
    }
}
