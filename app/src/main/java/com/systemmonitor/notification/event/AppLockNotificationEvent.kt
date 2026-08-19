package com.systemmonitor.notification.event

import com.systemmonitor.notification.NotificationPriority

data class AppLockNotificationEvent(
    val title: String,
    val message: String,
    val appName: String,
    val priority: NotificationPriority = NotificationPriority.HIGH
)
