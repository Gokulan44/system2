package com.systemmonitor.applock.settings

import android.content.Context
import android.content.SharedPreferences
import com.systemmonitor.applock.model.LockMethod
import com.systemmonitor.applock.model.LockSettings
import com.systemmonitor.applock.model.LockTiming
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("applock_settings_prefs", Context.MODE_PRIVATE)

    fun getSettings(): LockSettings {
        val methodStr = prefs.getString("lock_method", LockMethod.PIN.name) ?: LockMethod.PIN.name
        val timingStr = prefs.getString("lock_timing", LockTiming.IMMEDIATELY.name) ?: LockTiming.IMMEDIATELY.name
        return LockSettings(
            lockMethod = try { LockMethod.valueOf(methodStr) } catch (e: Exception) { LockMethod.PIN },
            lockTiming = try { LockTiming.valueOf(timingStr) } catch (e: Exception) { LockTiming.IMMEDIATELY },
            biometricEnabled = prefs.getBoolean("biometric_enabled", true),
            lockOnScreenOff = prefs.getBoolean("lock_on_screen_off", true),
            sessionTimeoutSeconds = prefs.getInt("session_timeout", 30),
            maxFailedAttempts = prefs.getInt("max_failed_attempts", 5),
            startAfterReboot = prefs.getBoolean("start_after_reboot", true),
            notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        )
    }

    fun saveSettings(settings: LockSettings) {
        prefs.edit()
            .putString("lock_method", settings.lockMethod.name)
            .putString("lock_timing", settings.lockTiming.name)
            .putBoolean("biometric_enabled", settings.biometricEnabled)
            .putBoolean("lock_on_screen_off", settings.lockOnScreenOff)
            .putInt("session_timeout", settings.sessionTimeoutSeconds)
            .putInt("max_failed_attempts", settings.maxFailedAttempts)
            .putBoolean("start_after_reboot", settings.startAfterReboot)
            .putBoolean("notifications_enabled", settings.notificationsEnabled)
            .apply()
    }
}
