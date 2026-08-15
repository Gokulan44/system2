package com.systemmonitor.commands

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandHistory(
    @PrimaryKey val requestId: String,
    val commandType: String,
    val targetDeviceId: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val executionTimeMs: Long = 0L
)
