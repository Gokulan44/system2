package com.systemmonitor.applock.manager

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.systemmonitor.applock.AppLockService
import com.systemmonitor.applock.data.database.LockedAppDao
import com.systemmonitor.applock.data.entity.LockedAppEntity
import com.systemmonitor.applock.security.CredentialStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.systemmonitor.applock.authentication.PinManager
import com.systemmonitor.applock.authentication.PatternManager
import com.systemmonitor.applock.authentication.PasswordManager

@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockedAppDao: LockedAppDao,
    private val credentialStorage: CredentialStorage,
    val pinManager: PinManager,
    val patternManager: PatternManager,
    val passwordManager: PasswordManager
) {
    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        managerScope.launch {
            val lockedApps = lockedAppDao.getAllLockedApps().first()
            if (lockedApps.isNotEmpty()) {
                startLockService()
            }
        }
    }

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

    fun launchUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching {
            intent.data = android.net.Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }.recover {
            val fallbackIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallbackIntent)
        }
    }

    fun launchOverlaySettings() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching {
            context.startActivity(intent)
        }
    }

    private val unlockedSessions = ConcurrentHashMap.newKeySet<String>()
    private val serviceRunning = AtomicBoolean(false)

    fun isServiceRunning(): Boolean = serviceRunning.get()

    fun getLockedApps(): Flow<List<LockedAppEntity>> = lockedAppDao.getAllLockedApps()

    suspend fun isAppProtected(packageName: String): Boolean {
        if (unlockedSessions.contains(packageName)) return false
        return lockedAppDao.isAppLocked(packageName)
    }

    suspend fun setAppLocked(packageName: String, appName: String, isLocked: Boolean) {
        if (isLocked) {
            lockedAppDao.insertLockedApp(
                LockedAppEntity(packageName = packageName, appName = appName, lockedAt = System.currentTimeMillis())
            )
            startLockService()
        } else {
            unlockedSessions.remove(packageName)
            lockedAppDao.deleteLockedApp(packageName)
            if (lockedAppDao.getAllLockedApps().first().isEmpty()) {
                stopLockService()
            }
        }
    }

    /**
     * The lock service runs ONLY at reboot today — nothing starts it when the
     * user actually locks an app, so protection is dead in-session. This starts
     * it on the first locked app and stops it when the last one is unlocked.
     */
    fun startLockService() {
        if (!serviceRunning.compareAndSet(false, true)) return
        val intent = Intent(context, AppLockService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopLockService() {
        if (!serviceRunning.compareAndSet(true, false)) return
        context.stopService(Intent(context, AppLockService::class.java))
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

    fun setServiceRunning(running: Boolean) {
        serviceRunning.set(running)
    }

    fun hasCredential(): Boolean = credentialStorage.hasCredential()

    fun getLockMethod(): String = credentialStorage.getLockType()

    fun verifyPasscode(input: String): Boolean {
        val method = getLockMethod()
        return when {
            method.equals("PIN", ignoreCase = true) || method.equals("Biometric Lock", ignoreCase = true) -> {
                pinManager.verifyPin(input) is com.systemmonitor.applock.authentication.AuthenticationResult.Success
            }
            method.equals("Pattern Lock", ignoreCase = true) -> {
                val points = input.split("-").mapNotNull { it.toIntOrNull() }
                patternManager.verifyPattern(points) is com.systemmonitor.applock.authentication.AuthenticationResult.Success
            }
            method.equals("Password Lock", ignoreCase = true) -> {
                passwordManager.verifyPassword(input) is com.systemmonitor.applock.authentication.AuthenticationResult.Success
            }
            else -> credentialStorage.verifyCredential(input)
        }
    }

    fun setLockPasscode(passcode: String, type: String) {
        credentialStorage.saveCredential(passcode, type)
        when {
            type.equals("PIN", ignoreCase = true) || type.equals("Biometric Lock", ignoreCase = true) -> {
                pinManager.createPin(passcode)
            }
            type.equals("Pattern Lock", ignoreCase = true) -> {
                val points = passcode.split("-").mapNotNull { it.toIntOrNull() }
                patternManager.createPattern(points)
            }
            type.equals("Password Lock", ignoreCase = true) -> {
                passwordManager.createPassword(passcode)
            }
        }
    }

    fun saveRecoveryCode(code: String) {
        credentialStorage.saveRecoveryCode(code)
    }

    fun verifyRecoveryCode(code: String): Boolean {
        return credentialStorage.verifyRecoveryCode(code)
    }

    fun hasRecoveryCode(): Boolean = credentialStorage.hasRecoveryCode()

    fun generateRecoveryCode(): String = credentialStorage.generateRecoveryCode()
}