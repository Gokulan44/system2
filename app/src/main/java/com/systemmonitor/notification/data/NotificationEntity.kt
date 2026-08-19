package com.systemmonitor.notification.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_notifications_table")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val priority: String = "DEFAULT",
    val actionData: String? = null
)
