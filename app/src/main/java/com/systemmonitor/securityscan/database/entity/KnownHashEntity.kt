package com.systemmonitor.securityscan.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_known_hashes")
data class KnownHashEntity(
    @PrimaryKey val sha256: String,
    val type: String, // CLEAN or MALWARE
    val appName: String,
    val threatName: String?
)
