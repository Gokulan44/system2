package com.systemmonitor.features.security.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_scan_table")
data class SecurityScanEntity(
    @PrimaryKey val scanId: Long,
    val timestamp: Long,
    val score: Int,
    val rating: String,
    val issuesFoundCount: Int,
    val scannedItemsCount: Int,
    val durationMs: Long
)

@Entity(tableName = "threat_table")
data class ThreatEntity(
    @PrimaryKey val id: String,
    val scanId: Long,
    val title: String,
    val description: String,
    val packageName: String?,
    val severity: String,
    val category: String,
    val recommendedAction: String
)
