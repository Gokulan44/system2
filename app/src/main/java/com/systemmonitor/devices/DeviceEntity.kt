package com.systemmonitor.devices

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paired_devices")
data class DeviceEntity(
    @PrimaryKey val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int = 8765,
    val macAddress: String = "",
    val lastSeen: Long = System.currentTimeMillis(),
    val isPrimary: Boolean = false,
    val isRevoked: Boolean = false
)
