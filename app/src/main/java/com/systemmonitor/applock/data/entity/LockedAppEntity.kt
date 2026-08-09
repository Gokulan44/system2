package com.systemmonitor.applock.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps_table")
data class LockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val lockedAt: Long
)
