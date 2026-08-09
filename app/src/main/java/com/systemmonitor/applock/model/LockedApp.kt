package com.systemmonitor.applock.model

import com.systemmonitor.applock.database.LockedAppEntity

data class LockedApp(
    val packageName: String,
    val appName: String,
    val lockedAt: Long = System.currentTimeMillis(),
    val isSystemApp: Boolean = false
) {
    fun toEntity(): LockedAppEntity {
        return LockedAppEntity(
            packageName = packageName,
            appName = appName,
            enabled = true
        )
    }
}
