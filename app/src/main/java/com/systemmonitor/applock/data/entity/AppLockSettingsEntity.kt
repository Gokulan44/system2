package com.systemmonitor.applock.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applock_settings_table")
data class AppLockSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val lockMethod: String = "PIN",
    val lockImmediately: Boolean = true,
    val biometricEnabled: Boolean = true,
    val lockOnScreenOff: Boolean = true,
    val sessionTimeout: Int = 30
)
