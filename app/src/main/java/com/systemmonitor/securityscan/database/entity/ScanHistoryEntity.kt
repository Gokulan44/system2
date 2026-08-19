package com.systemmonitor.securityscan.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scanTarget: String,
    val targetName: String,
    val timestamp: Long,
    val score: Int,
    val verdict: String,
    val scannedItemsCount: Int,
    val durationMs: Long
)