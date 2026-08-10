package com.systemmonitor.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "laptops")
data class LaptopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int,
    val os: String,
    val status: String,
    val isLocalConnection: Boolean,
    val accessToken: String?,
    val lastSeen: Long,
    @ColumnInfo(name = "connectionMode", defaultValue = "LOCAL")
    val connectionMode: String = "LOCAL"
)
