package com.systemmonitor.notification.event

import com.systemmonitor.notification.NotificationPriority

data class SecurityNotificationEvent(
    val title: String,
    val message: String,
    val threatLevel: String,
    val priority: NotificationPriority = NotificationPriority.HIGH
)
