package com.systemmonitor.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_readings")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val totalMb: Long,
    val availableMb: Long,
    val usedMb: Long,
    val thresholdMb: Long,
    val isLowMemory: Boolean
)
