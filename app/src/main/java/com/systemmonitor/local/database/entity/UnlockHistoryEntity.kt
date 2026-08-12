package com.systemmonitor.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlock_history")
data class UnlockHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val laptopId: String,
    val timestamp: Long,
    val result: String, // SUCCESS, REJECTED, FAILED
    val method: String, // FINGERPRINT, PIN
    val reason: String? = null
)
