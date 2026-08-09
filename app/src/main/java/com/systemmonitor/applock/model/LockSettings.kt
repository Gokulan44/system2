package com.systemmonitor.applock.model

import com.systemmonitor.applock.database.LockSettingsEntity

data class LockSettings(
    val lockMethod: LockMethod = LockMethod.PIN,
    val lockTiming: LockTiming = LockTiming.IMMEDIATELY,
    val biometricEnabled: Boolean = true,
    val lockOnScreenOff: Boolean = true,
    val sessionTimeoutSeconds: Int = 30,
    val maxFailedAttempts: Int = 5,
    val startAfterReboot: Boolean = true,
    val notificationsEnabled: Boolean = true
) {
    fun toEntity(): LockSettingsEntity {
        return LockSettingsEntity(
            lockMethod = lockMethod.name,
            autoLockDelay = when (lockTiming) {
                LockTiming.IMMEDIATELY -> 0L
                LockTiming.AFTER_30_SECONDS -> 30_000L
                LockTiming.AFTER_1_MINUTE -> 60_000L
                LockTiming.AFTER_SCREEN_OFF -> -1L
            },
            biometricEnabled = biometricEnabled
        )
    }
}
