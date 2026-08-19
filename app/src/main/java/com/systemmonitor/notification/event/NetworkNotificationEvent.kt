package com.systemmonitor.notification.event

import com.systemmonitor.notification.NotificationPriority

data class NetworkNotificationEvent(
    val title: String,
    val message: String,
    val ssid: String,
    val priority: NotificationPriority = NotificationPriority.DEFAULT
)
