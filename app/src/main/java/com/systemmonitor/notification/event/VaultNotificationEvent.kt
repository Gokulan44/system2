package com.systemmonitor.notification.event

import com.systemmonitor.notification.NotificationPriority

data class VaultNotificationEvent(
    val title: String,
    val message: String,
    val fileCount: Int,
    val priority: NotificationPriority = NotificationPriority.LOW
)
