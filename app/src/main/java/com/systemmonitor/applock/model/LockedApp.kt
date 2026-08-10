package com.systemmonitor.applock.model

import com.systemmonitor.applock.data.entity.LockedAppEntity as DataLockedAppEntity

data class LockedApp(
    val packageName: String,
    val appName: String,
    val lockedAt: Long = System.currentTimeMillis(),
    val isSystemApp: Boolean = false
) {
    fun toEntity(): DataLockedAppEntity {
        return DataLockedAppEntity(
            packageName = packageName,
            appName = appName,
            lockedAt = lockedAt
        )
    }
}