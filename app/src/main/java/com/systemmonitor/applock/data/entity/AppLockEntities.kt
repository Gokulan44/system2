package com.systemmonitor.applock.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps_table")
data class LockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val lockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "authentication_logs_table")
data class AuthenticationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val result: String,
    val authenticationMethod: String,
    val attemptCount: Int
)

@Entity(tableName = "applock_settings_table")
data class AppLockSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val lockMethod: String,
    val lockImmediately: Boolean,
    val biometricEnabled: Boolean,
    val lockOnScreenOff: Boolean,
    val sessionTimeout: Int
)
