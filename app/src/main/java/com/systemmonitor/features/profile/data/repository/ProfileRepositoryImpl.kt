package com.systemmonitor.features.profile.data.repository

import com.systemmonitor.features.profile.data.dao.*
import com.systemmonitor.features.profile.data.entity.*
import com.systemmonitor.features.profile.domain.model.*
import com.systemmonitor.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val loginHistoryDao: LoginHistoryDao,
    private val activityHistoryDao: ActivityHistoryDao,
    private val deviceSessionDao: DeviceSessionDao,
    private val notificationPreferenceDao: NotificationPreferenceDao
) : ProfileRepository {

    private val _privacyState = MutableStateFlow(PrivacySettings())
    private val _prefsState = MutableStateFlow(UserPreferences())

    override fun getUserProfile(): Flow<UserProfile> {
        return userProfileDao.getUserProfile().map { entity ->
            entity?.let {
                UserProfile(
                    id = it.id,
                    fullName = it.fullName,
                    email = it.email,
                    phone = it.phone,
                    country = it.country,
                    avatarUrl = it.avatarUrl,
                    joinedTimestamp = it.joinedTimestamp
                )
            } ?: UserProfile()
        }
    }

    override suspend fun updateProfile(profile: UserProfile): Boolean {
        userProfileDao.insertProfile(
            UserProfileEntity(
                id = profile.id,
                fullName = profile.fullName,
                email = profile.email,
                phone = profile.phone,
                country = profile.country,
                avatarUrl = profile.avatarUrl,
                joinedTimestamp = profile.joinedTimestamp
            )
        )
        return true
    }

    override suspend fun uploadProfilePhoto(uriString: String): Boolean {
        val current = UserProfile(avatarUrl = uriString)
        updateProfile(current)
        return true
    }

    override fun getConnectedDevices(): Flow<List<ConnectedDevice>> {
        return deviceSessionDao.getDeviceSessions().map { entities ->
            if (entities.isEmpty()) {
                listOf(
                    ConnectedDevice("dev_01", "This Smartphone", "Android Mobile", "Android 14", "Active Now", true),
                    ConnectedDevice("dev_02", "Work Laptop (Win11)", "Windows Workstation", "Windows 11 Pro", "2 hours ago", false)
                )
            } else {
                entities.map {
                    ConnectedDevice(it.deviceId, it.deviceName, it.deviceType, it.osVersion, it.lastActive, it.isCurrentDevice)
                }
            }
        }
    }

    override suspend fun removeDevice(deviceId: String): Boolean {
        deviceSessionDao.deleteDevice(deviceId)
        return true
    }

    override fun getLoginHistory(): Flow<List<LoginSession>> {
        return loginHistoryDao.getLoginHistory().map { entities ->
            if (entities.isEmpty()) {
                listOf(
                    LoginSession("sess_01", "Samsung Galaxy S24", "192.168.1.42", "New York, USA", "Today, 14:22", true),
                    LoginSession("sess_02", "Dell XPS 15 Laptop", "192.168.1.108", "New York, USA", "Yesterday, 09:15", false)
                )
            } else {
                entities.map { LoginSession(it.sessionId, it.deviceName, it.ipAddress, it.location, it.loginTime, it.isCurrentSession) }
            }
        }
    }

    override fun getActivityHistory(): Flow<List<ActivityRecord>> {
        return activityHistoryDao.getActivityHistory().map { entities ->
            if (entities.isEmpty()) {
                listOf(
                    ActivityRecord("act_01", "Full Security Scan", "Security", "10 minutes ago", "0 threats detected"),
                    ActivityRecord("act_02", "App Lock Activated", "AppLock", "1 hour ago", "WhatsApp locked"),
                    ActivityRecord("act_03", "Laptop Paired", "Device", "2 hours ago", "Work Laptop connected")
                )
            } else {
                entities.map { ActivityRecord(it.id, it.action, it.category, it.timestamp, it.details) }
            }
        }
    }

    override fun getNotificationPreferences(): Flow<NotificationPreferences> {
        return notificationPreferenceDao.getPreferences().map { entity ->
            entity?.let {
                NotificationPreferences(
                    securityAlerts = it.securityAlerts,
                    scanCompleted = it.scanCompleted,
                    deviceConnected = it.deviceConnected,
                    deviceDisconnected = it.deviceDisconnected,
                    batteryAlerts = it.batteryAlerts,
                    appLockAlerts = it.appLockAlerts,
                    reportNotifications = it.reportNotifications,
                    systemNotifications = it.systemNotifications
                )
            } ?: NotificationPreferences()
        }
    }

    override suspend fun updateNotificationPreferences(prefs: NotificationPreferences): Boolean {
        notificationPreferenceDao.insertPreferences(
            NotificationPreferenceEntity(
                id = 1,
                securityAlerts = prefs.securityAlerts,
                scanCompleted = prefs.scanCompleted,
                deviceConnected = prefs.deviceConnected,
                deviceDisconnected = prefs.deviceDisconnected,
                batteryAlerts = prefs.batteryAlerts,
                appLockAlerts = prefs.appLockAlerts,
                reportNotifications = prefs.reportNotifications,
                systemNotifications = prefs.systemNotifications
            )
        )
        return true
    }

    override fun getPrivacySettings(): Flow<PrivacySettings> = _privacyState.asStateFlow()

    override suspend fun updatePrivacySettings(privacy: PrivacySettings): Boolean {
        _privacyState.value = privacy
        return true
    }

    override fun getUserPreferences(): Flow<UserPreferences> = _prefsState.asStateFlow()

    override suspend fun updateUserPreferences(prefs: UserPreferences): Boolean {
        _prefsState.value = prefs
        return true
    }

    override suspend fun signOut(): Boolean = true

    override suspend fun deleteAccount(): Boolean = true
}
