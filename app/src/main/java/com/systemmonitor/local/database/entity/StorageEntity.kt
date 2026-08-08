package com.systemmonitor.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storage_readings")
data class StorageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long
)
