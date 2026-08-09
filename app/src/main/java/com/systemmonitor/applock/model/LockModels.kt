package com.systemmonitor.applock.model

enum class LockMethod {
    PIN,
    PATTERN,
    PASSWORD,
    BIOMETRIC
}

enum class LockTiming {
    IMMEDIATELY,
    AFTER_30_SECONDS,
    AFTER_1_MINUTE,
    AFTER_SCREEN_OFF
}

data class LockedApp(
    val packageName: String,
    val appName: String,
    val lockedAt: Long = System.currentTimeMillis(),
    val isSystemApp: Boolean = false
)

enum class LockState {
    LOCKED,
    UNLOCKED,
    TIMED_OUT
}

data class LockSettings(
    val lockMethod: LockMethod = LockMethod.PIN,
    val lockTiming: LockTiming = LockTiming.IMMEDIATELY,
    val biometricEnabled: Boolean = true,
    val lockOnScreenOff: Boolean = true,
    val sessionTimeoutSeconds: Int = 30,
    val maxFailedAttempts: Int = 5,
    val startAfterReboot: Boolean = true,
    val notificationsEnabled: Boolean = true
)
