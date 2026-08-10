package com.systemmonitor.applock

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.systemmonitor.applock.manager.AppLockManager
import com.systemmonitor.applock.ui.LockOverlayActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class AppLockService : Service() {

    @Inject lateinit var detector: ForegroundAppDetector
    @Inject lateinit var protectedAppManager: ProtectedAppManager
    @Inject lateinit var sessionManager: LockSessionManager
    @Inject lateinit var notificationHelper: AppLockNotification
    @Inject lateinit var appLockManager: AppLockManager

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var lastForegroundPackage: String? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                lastForegroundPackage = null
                if (sessionManager.getLockOnScreenOff()) {
                    sessionManager.clearSession()
                    appLockManager.lockAllSessions()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        appLockManager.setServiceRunning(true)
        startForeground(AppLockNotification.NOTIFICATION_ID, notificationHelper.getNotification())

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver, filter)

        startMonitoring()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            // Stop the service if no apps are locked
            val lockedApps = protectedAppManager.lockedApps.first()
            if (lockedApps.isEmpty()) {
                withContext(Dispatchers.Main) {
                    stopSelf()
                }
                return@launch
            }

            while (isActive) {
                val fgPackage = detector.getForegroundPackage()
                if (fgPackage != null && fgPackage != lastForegroundPackage && fgPackage != packageName) {
                    lastForegroundPackage = fgPackage
                    if (protectedAppManager.isProtected(fgPackage) && !sessionManager.isTemporarilyUnlocked(fgPackage)) {
                        withContext(Dispatchers.Main) {
                            val intent = Intent(applicationContext, LockOverlayActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                putExtra("target_package", fgPackage)
                            }
                            startActivity(intent)
                        }
                    }
                }
                delay(500)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        appLockManager.setServiceRunning(false)
        runCatching { unregisterReceiver(screenReceiver) }
        serviceScope.cancel()
        super.onDestroy()
    }
}

