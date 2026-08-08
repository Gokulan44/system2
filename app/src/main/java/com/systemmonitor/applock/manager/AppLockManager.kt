package com.systemmonitor.applock.manager

import com.systemmonitor.applock.database.AppLockDao
import com.systemmonitor.applock.database.LockedAppEntity
import com.systemmonitor.applock.security.CredentialStorage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(
    private val appLockDao: AppLockDao,
    private val credentialStorage: CredentialStorage
) {
    private val unlockedSessions = mutableSetOf<String>()

    fun getLockedApps(): Flow<List<LockedAppEntity>> = appLockDao.getLockedApps()

    suspend fun isAppProtected(packageName: String): Boolean {
        if (unlockedSessions.contains(packageName)) return false
        return appLockDao.isAppLocked(packageName)
    }

    suspend fun setAppLocked(packageName: String, appName: String, isLocked: Boolean) {
        if (isLocked) {
            appLockDao.lockApp(LockedAppEntity(packageName = packageName, appName = appName, enabled = true))
        } else {
            appLockDao.unlockApp(packageName)
        }
    }

    fun markSessionUnlocked(packageName: String) {
        unlockedSessions.add(packageName)
    }

    fun lockAllSessions() {
        unlockedSessions.clear()
    }

    fun verifyPasscode(input: String): Boolean {
        return credentialStorage.verifyCredential(input)
    }

    fun setLockPasscode(passcode: String, type: String) {
        credentialStorage.saveCredential(passcode, type)
    }

    fun getLockMethod(): String = credentialStorage.getLockType()
}
