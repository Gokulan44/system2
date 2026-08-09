package com.systemmonitor.features.profile.domain.model

data class UserProfile(
    val id: String = "user_001",
    val fullName: String = "Gopal Kumar",
    val email: String = "gopal@systemmonitor.com",
    val phone: String = "+1 234 567 890",
    val country: String = "United States",
    val avatarUrl: String? = null,
    val joinedTimestamp: Long = System.currentTimeMillis()
)

data class AccountSecurity(
    val isBiometricEnabled: Boolean = true,
    val isTwoFactorEnabled: Boolean = false,
    val isLoginAlertsEnabled: Boolean = true,
    val activeSessionsCount: Int = 2
)

data class ConnectedDevice(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val osVersion: String,
    val lastActive: String,
    val isCurrentDevice: Boolean = false
)

data class LoginSession(
    val sessionId: String,
    val deviceName: String,
    val ipAddress: String,
    val location: String,
    val loginTime: String,
    val isCurrentSession: Boolean = false
)

data class ActivityRecord(
    val id: String,
    val action: String,
    val category: String,
    val timestamp: String,
    val details: String
)

data class NotificationPreferences(
    val securityAlerts: Boolean = true,
    val scanCompleted: Boolean = true,
    val deviceConnected: Boolean = true,
    val deviceDisconnected: Boolean = true,
    val batteryAlerts: Boolean = true,
    val appLockAlerts: Boolean = true,
    val reportNotifications: Boolean = true,
    val systemNotifications: Boolean = true
)

data class PrivacySettings(
    val appPermissionsGranted: Int = 18,
    val dataCollectionEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = true,
    val exportDataAvailable: Boolean = true
)

data class UserPreferences(
    val theme: String = "Dark System",
    val language: String = "English (US)",
    val startupBehavior: String = "Overview Dashboard",
    val defaultDashboard: String = "System Monitor",
    val animationsEnabled: Boolean = true
)
