package com.systemmonitor.applock.data.repository

import com.systemmonitor.applock.database.AppLockDao
import com.systemmonitor.applock.database.LockedAppEntity
import com.systemmonitor.applock.database.UnlockHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockDataRepository @Inject constructor(
    private val appLockDao: AppLockDao
) {
    fun getLockedApps(): Flow<List<LockedAppEntity>> = appLockDao.getLockedApps()

    suspend fun lockApp(packageName: String, appName: String) {
        appLockDao.lockApp(LockedAppEntity(packageName = packageName, appName = appName, enabled = true))
    }

    suspend fun unlockApp(packageName: String) {
        appLockDao.unlockApp(packageName)
    }

    suspend fun isAppLocked(packageName: String): Boolean {
        return appLockDao.isAppLocked(packageName)
    }

    fun getAuthenticationLogs(): Flow<List<UnlockHistoryEntity>> = appLockDao.getUnlockHistory()

    suspend fun logAuthenticationAttempt(packageName: String, success: Boolean, method: String) {
        appLockDao.logUnlockAttempt(UnlockHistoryEntity(appPackage = packageName, result = if (success) "SUCCESS" else "FAILED"))
    }
}
