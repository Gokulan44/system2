package com.systemmonitor.securityanalysis.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detected_threats")
data class ThreatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scanId: Long,
    val threatName: String,
    val severity: String, // Low, Medium, High, Critical
    val category: String, // Malware, Suspicious Permission, Malicious JS, Sideloaded
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
