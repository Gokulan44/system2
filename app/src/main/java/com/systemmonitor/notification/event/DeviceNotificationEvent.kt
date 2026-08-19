package com.systemmonitor.notification.event

import com.systemmonitor.notification.NotificationPriority

data class DeviceNotificationEvent(
    val title: String,
    val message: String,
    val deviceName: String,
    val priority: NotificationPriority = NotificationPriority.DEFAULT
)
