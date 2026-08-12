package com.systemmonitor.features.remotepermission.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_results")
data class DownloadResultEntity(
    @PrimaryKey val requestId: String,
    val status: String,
    val filePath: String,
    val completedAt: Long
)
