package com.systemmonitor.securityscan.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_scan_findings")
data class FindingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scanId: Long,
    val category: String,
    val severity: String,
    val title: String,
    val details: String,
    val componentName: String?
)