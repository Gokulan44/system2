package com.systemmonitor.applock.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "authentication_logs_table")
data class AuthenticationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val timestamp: Long,
    val result: String,
    val authenticationMethod: String,
    val attemptCount: Int
)
