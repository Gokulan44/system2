package com.systemmonitor.features.remotepermission.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "permission_requests")
data class PermissionRequestEntity(
    @PrimaryKey val requestId: String,
    val laptopId: String,
    val resourceId: String,
    val resourceName: String,
    val resourceType: String,
    val fileSize: Long,
    val requestedOperation: String,
    val createdAt: Long,
    val expiresAt: Long,
    val requestNonce: String,
    val status: String
)
