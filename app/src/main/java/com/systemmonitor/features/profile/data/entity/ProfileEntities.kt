package com.systemmonitor.features.profile.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile_table")
data class UserProfileEntity(
    @PrimaryKey val id: String = "user_001",
    val fullName: String,
    val email: String,
    val phone: String,
    val country: String,
    val avatarUrl: String?,
    val joinedTimestamp: Long
)

@Entity(tableName = "login_history_table")
data class LoginHistoryEntity(
    @PrimaryKey val sessionId: String,
    val deviceName: String,
    val ipAddress: String,
    val location: String,
    val loginTime: String,
    val isCurrentSession: Boolean
)

@Entity(tableName = "activity_history_table")
data class ActivityHistoryEntity(
    @PrimaryKey val id: String,
    val action: String,
    val category: String,
    val timestamp: String,
    val details: String
)

@Entity(tableName = "device_session_table")
data class DeviceSessionEntity(
    @PrimaryKey val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val osVersion: String,
    val lastActive: String,
    val isCurrentDevice: Boolean
)

@Entity(tableName = "notification_preference_table")
data class NotificationPreferenceEntity(
    @PrimaryKey val id: Int = 1,
    val securityAlerts: Boolean,
    val scanCompleted: Boolean,
    val deviceConnected: Boolean,
    val deviceDisconnected: Boolean,
    val batteryAlerts: Boolean,
    val appLockAlerts: Boolean,
    val reportNotifications: Boolean,
    val systemNotifications: Boolean
)
