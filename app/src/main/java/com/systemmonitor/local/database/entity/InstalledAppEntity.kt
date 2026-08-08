package com.systemmonitor.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_apps")
data class InstalledAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val versionName: String?,
    val installerPackageName: String?,
    val isSystemApp: Boolean,
    val totalPermissions: Int,
    val dangerousPermissions: Int,
    val lastUpdatedTimestamp: Long,
    val lastScannedTimestamp: Long
)
