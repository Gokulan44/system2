package com.systemmonitor.features.remotepermission.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "approval_events")
data class ApprovalEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requestId: String,
    val timestamp: Long,
    val token: String,
    val signature: String
)
