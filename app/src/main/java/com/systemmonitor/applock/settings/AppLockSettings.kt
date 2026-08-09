package com.systemmonitor.applock.settings

import com.systemmonitor.applock.model.LockMethod
import com.systemmonitor.applock.model.LockTiming

import com.systemmonitor.applock.model.LockSettings

data class AppLockSettings(
    val lockMethod: LockMethod = LockMethod.PIN,
    val lockTiming: LockTiming = LockTiming.IMMEDIATELY,
    val biometricEnabled: Boolean = true,
    val lockOnScreenOff: Boolean = true,
    val sessionTimeoutSeconds: Int = 30
) {
    fun toLockSettings(): LockSettings {
        return LockSettings(
            lockMethod = lockMethod,
            lockTiming = lockTiming,
            biometricEnabled = biometricEnabled,
            lockOnScreenOff = lockOnScreenOff,
            sessionTimeoutSeconds = sessionTimeoutSeconds
        )
    }
}
