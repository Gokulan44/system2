package com.systemmonitor.securityanalysis.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val sha256: String,
    val mimeType: String,
    val riskScore: Int,
    val status: String, // Safe, Suspicious, Malicious
    val threatCategory: String,
    val timestamp: Long = System.currentTimeMillis()
)
