package com.systemmonitor.applock.manager

import com.systemmonitor.applock.database.AppLockDao
import com.systemmonitor.applock.database.LockedAppEntity
import com.systemmonitor.applock.security.CredentialStorage
import kotlinx.coroutines.flow.Flow
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appLockDao: AppLockDao,
    private val credentialStorage: CredentialStorage
) {
    @Suppress("DEPRECATION")
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun getUsageAccessIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }

    fun getOverlayPermissionIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }
    private val unlockedSessions = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    fun getLockedApps(): Flow<List<LockedAppEntity>> = appLockDao.getLockedApps()

    suspend fun isAppProtected(packageName: String): Boolean {
        if (unlockedSessions.contains(packageName)) return false
        return appLockDao.isAppLocked(packageName)
    }

    suspend fun setAppLocked(packageName: String, appName: String, isLocked: Boolean) {
        if (isLocked) {
            appLockDao.lockApp(LockedAppEntity(packageName = packageName, appName = appName, enabled = true))
        } else {
            unlockedSessions.remove(packageName)
            appLockDao.unlockApp(packageName)
        }
    }

    fun markSessionUnlocked(packageName: String) {
        unlockedSessions.add(packageName)
    }

    fun relockApp(packageName: String) {
        unlockedSessions.remove(packageName)
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
