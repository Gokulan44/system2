package com.systemmonitor.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wifi_readings")
data class WifiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val ssid: String?,          // null if location permission not granted
    val bssid: String?,
    val rssiDbm: Int,
    val linkSpeedMbps: Int,
    val frequencyMhz: Int
)
