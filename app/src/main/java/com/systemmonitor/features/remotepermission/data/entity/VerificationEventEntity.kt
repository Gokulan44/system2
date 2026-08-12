package com.systemmonitor.features.remotepermission.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verification_events")
data class VerificationEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requestId: String,
    val timestamp: Long,
    val method: String,
    val result: String
)
