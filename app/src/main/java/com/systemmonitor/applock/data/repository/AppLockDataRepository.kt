package com.systemmonitor.applock.data.repository

import com.systemmonitor.applock.data.database.AuthenticationLogDao
import com.systemmonitor.applock.data.database.LockedAppDao
import com.systemmonitor.applock.data.entity.AuthenticationLogEntity
import com.systemmonitor.applock.data.entity.LockedAppEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AppLockDataRepository @Inject constructor(
    private val lockedAppDao: LockedAppDao,
    private val authLogDao: AuthenticationLogDao
) {
    fun getLockedApps(): Flow<List<LockedAppEntity>> = lockedAppDao.getAllLockedApps()

    suspend fun lockApp(packageName: String, appName: String) {
        lockedAppDao.insertLockedApp(
            LockedAppEntity(packageName = packageName, appName = appName, lockedAt = System.currentTimeMillis())
        )
    }

    suspend fun unlockApp(packageName: String) {
        lockedAppDao.deleteLockedApp(packageName)
    }

    suspend fun isAppLocked(packageName: String): Boolean {
        return lockedAppDao.isAppLocked(packageName)
    }

    fun getAuthenticationLogs(): Flow<List<AuthenticationLogEntity>> = authLogDao.getAllLogs()

    suspend fun logAuthenticationAttempt(packageName: String, success: Boolean, method: String) {
        authLogDao.insertLog(
            AuthenticationLogEntity(
                packageName = packageName,
                timestamp = System.currentTimeMillis(),
                result = if (success) "SUCCESS" else "FAILED",
                authenticationMethod = method,
                attemptCount = 1
            )
        )
    }
}