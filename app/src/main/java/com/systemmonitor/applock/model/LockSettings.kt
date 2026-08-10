package com.systemmonitor.applock.model

import com.systemmonitor.applock.data.entity.AppLockSettingsEntity

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
    fun toEntity(): AppLockSettingsEntity {
        return AppLockSettingsEntity(
            lockMethod = lockMethod.name,
            lockImmediately = lockTiming == LockTiming.IMMEDIATELY,
            biometricEnabled = biometricEnabled,
            lockOnScreenOff = lockOnScreenOff,
            sessionTimeout = sessionTimeoutSeconds
        )
    }
}