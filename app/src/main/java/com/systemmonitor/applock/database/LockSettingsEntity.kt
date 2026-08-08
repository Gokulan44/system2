package com.systemmonitor.applock.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lock_settings")
data class LockSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "lock_method")
    val lockMethod: String = "PIN", // PIN, Pattern, Password, Biometric
    @ColumnInfo(name = "auto_lock_delay")
    val autoLockDelay: Long = 0, // Immediately (0), 5 mins, screen off
    @ColumnInfo(name = "biometric_enabled")
    val biometricEnabled: Boolean = true
)
