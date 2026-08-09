package com.systemmonitor.applock

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.systemmonitor.applock.ui.LockOverlayActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class AppLockService : Service() {

    @Inject lateinit var detector: ForegroundAppDetector
    @Inject lateinit var protectedAppManager: ProtectedAppManager
    @Inject lateinit var sessionManager: LockSessionManager
    @Inject lateinit var notificationHelper: AppLockNotification

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var lastForegroundPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(AppLockNotification.NOTIFICATION_ID, notificationHelper.getNotification())
        startMonitoring()
    }

    private fun startMonitoring() {
        serviceScope.launch {
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
        serviceScope.cancel()
        super.onDestroy()
    }
}
