package com.systemmonitor.features.remotepermission.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_scan_results")
data class SecurityScanResultEntity(
    @PrimaryKey val requestId: String,
    val status: String,
    val sha256: String,
    val riskLevel: String,
    val details: String
)
