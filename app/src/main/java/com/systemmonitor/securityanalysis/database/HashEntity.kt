package com.systemmonitor.securityanalysis.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threat_signatures")
data class HashEntity(
    @PrimaryKey
    val sha256: String,
    val malwareFamily: String,
    val riskLevel: String, // High, Critical
    val description: String
)
