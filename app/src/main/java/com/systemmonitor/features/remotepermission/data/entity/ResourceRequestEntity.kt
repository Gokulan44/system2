package com.systemmonitor.features.remotepermission.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resource_requests")
data class ResourceRequestEntity(
    @PrimaryKey val resourceId: String,
    val name: String,
    val type: String,
    val sizeBytes: Long,
    val path: String?
)
