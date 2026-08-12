package com.systemmonitor.features.remotepermission.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "permission_history")
data class PermissionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requestId: String,
    val laptopId: String,
    val resourceName: String,
    val operation: String,
    val status: String,
    val timestamp: Long,
    val verificationMethod: String?
)
