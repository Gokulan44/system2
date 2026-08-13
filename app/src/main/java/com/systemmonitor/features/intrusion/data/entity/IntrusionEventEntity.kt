package com.systemmonitor.features.intrusion.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intrusion_events")
data class IntrusionEventEntity(
    @PrimaryKey val eventId: String,
    val laptopId: String,
    val timestamp: Long,
    val attemptCount: Int,
    val severity: String,
    val isRead: Boolean = false,
    val encryptedPhoto: String? = null,
    val photoHash: String? = null
)
