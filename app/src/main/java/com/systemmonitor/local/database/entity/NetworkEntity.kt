package com.systemmonitor.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_readings")
data class NetworkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val isConnected: Boolean,
    val transportType: String,   // "WIFI", "CELLULAR", "ETHERNET", "VPN", "NONE"
    val isMetered: Boolean,
    val downstreamKbps: Int,
    val upstreamKbps: Int
)
