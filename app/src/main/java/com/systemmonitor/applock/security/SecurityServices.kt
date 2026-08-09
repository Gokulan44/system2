package com.systemmonitor.applock.security

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("applock_secure_storage", Context.MODE_PRIVATE)

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String = ""): String {
        return prefs.getString(key, default) ?: default
    }
}

@Singleton
class SecurityPolicy @Inject constructor() {
    val maxFailedAttempts: Int = 5
    val lockoutDurationMs: Long = 30000L // 30 seconds lockout
}

@Singleton
class AntiTamperManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isDeviceAdminActive(): Boolean {
        return false // Device Admin active status check
    }
}

@Singleton
class IntrusionLogger @Inject constructor() {
    private val logs = mutableListOf<String>()

    fun logFailedAttempt(packageName: String) {
        logs.add("[${System.currentTimeMillis()}] Failed unlock attempt for $packageName")
    }

    fun getLogs(): List<String> = logs.toList()
}
