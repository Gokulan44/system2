package com.systemmonitor.securityanalysis.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quarantine_vault")
data class QuarantineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalFileName: String,
    val originalFilePath: String,
    val quarantineFilePath: String,
    val sha256: String,
    val reason: String,
    val isRestored: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
