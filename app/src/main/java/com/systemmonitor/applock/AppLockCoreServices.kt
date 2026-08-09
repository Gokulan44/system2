package com.systemmonitor.applock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.systemmonitor.applock.data.repository.AppLockDataRepository
import com.systemmonitor.applock.model.LockState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForegroundAppDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getForegroundPackage(): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null
        val time = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(time - 3000, time)
        val event = UsageEvents.Event()
        var currentApp: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentApp = event.packageName
            }
        }
        return currentApp
    }
}

@Singleton
class ProtectedAppManager @Inject constructor(
    private val repository: AppLockDataRepository
) {
    val lockedApps = repository.getLockedApps()

    suspend fun isProtected(packageName: String): Boolean {
        return repository.isAppLocked(packageName)
    }
}

@Singleton
class LockStateManager @Inject constructor() {
    private val _lockState = MutableStateFlow<LockState>(LockState.LOCKED)
    val lockState: StateFlow<LockState> = _lockState

    fun setUnlocked() {
        _lockState.value = LockState.UNLOCKED
    }

    fun setLocked() {
        _lockState.value = LockState.LOCKED
    }
}

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

@Singleton
class AppLockNotification @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "app_lock_channel"
        const val NOTIFICATION_ID = 1001
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Lock Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors applications and enforces security lock"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun getNotification(): android.app.Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("App Lock Protection Active")
            .setContentText("Monitoring protected applications in real-time")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}

class AppLockBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                val serviceIntent = Intent(it, AppLockService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.startForegroundService(serviceIntent)
                } else {
                    it.startService(serviceIntent)
                }
            }
        }
    }
}
