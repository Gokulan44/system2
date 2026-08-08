package com.systemmonitor.applock.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlock_history")
data class UnlockHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "app_package")
    val appPackage: String,
    @ColumnInfo(name = "result")
    val result: String, // SUCCESS, FAILED, CANCELLED
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "failure_count")
    val failureCount: Int = 0
)
