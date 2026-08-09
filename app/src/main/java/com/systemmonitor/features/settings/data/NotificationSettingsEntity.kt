package com.systemmonitor.features.settings.data

data class NotificationSettingsEntity(
    val masterNotificationsEnabled: Boolean = true,
    val securityAlerts: Boolean = true,
    val deviceAlerts: Boolean = true,
    val batteryAlerts: Boolean = true,
    val storageAlerts: Boolean = true,
    val networkAlerts: Boolean = true,
    val appUsageAlerts: Boolean = true,
    val reportNotifications: Boolean = true
)
