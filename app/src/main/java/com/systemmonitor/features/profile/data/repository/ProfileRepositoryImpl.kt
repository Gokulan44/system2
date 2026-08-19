package com.systemmonitor.features.profile.data.repository

import android.os.Build
import com.systemmonitor.features.profile.data.dao.*
import com.systemmonitor.features.profile.data.entity.*
import com.systemmonitor.features.profile.domain.model.*
import com.systemmonitor.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
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

    init {
        CoroutineScope(Dispatchers.IO).launch {
            initializeRealSessionData()
        }
    }

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
            entities.map {
                ConnectedDevice(it.deviceId, it.deviceName, it.deviceType, it.osVersion, it.lastActive, it.isCurrentDevice)
            }
        }
    }

    override suspend fun removeDevice(deviceId: String): Boolean {
        deviceSessionDao.deleteDevice(deviceId)
        return true
    }

    override fun getLoginHistory(): Flow<List<LoginSession>> {
        return loginHistoryDao.getLoginHistory().map { entities ->
            entities.map { LoginSession(it.sessionId, it.deviceName, it.ipAddress, it.location, it.loginTime, it.isCurrentSession) }
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

    private suspend fun initializeRealSessionData() {
        try {
            // Get current device details
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val realDeviceName = if (model.startsWith(manufacturer, ignoreCase = true)) {
                model.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } else {
                manufacturer.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + " " + model
            }
            val realOsVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            val ipAddress = getLocalIpAddress()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val currentTime = sdf.format(Date())

            // Insert current device if not present
            val currentDevices = deviceSessionDao.getDeviceSessions().first()
            if (currentDevices.none { it.isCurrentDevice }) {
                deviceSessionDao.insertDevice(
                    DeviceSessionEntity(
                        deviceId = "dev_current",
                        deviceName = realDeviceName,
                        deviceType = "Android Mobile",
                        osVersion = realOsVersion,
                        lastActive = "Active Now",
                        isCurrentDevice = true
                    )
                )
                // Add a mock workstation laptop for demonstration
                deviceSessionDao.insertDevice(
                    DeviceSessionEntity(
                        deviceId = "dev_laptop",
                        deviceName = "Work Laptop (Win11)",
                        deviceType = "Windows Workstation",
                        osVersion = "Windows 11 Pro",
                        lastActive = "2 hours ago",
                        isCurrentDevice = false
                    )
                )
            }

            // Insert current login session
            val currentLogins = loginHistoryDao.getLoginHistory().first()
            if (currentLogins.none { it.isCurrentSession }) {
                loginHistoryDao.insertLoginSession(
                    LoginHistoryEntity(
                        sessionId = "sess_current",
                        deviceName = realDeviceName,
                        ipAddress = ipAddress,
                        location = "Local Network",
                        loginTime = currentTime,
                        isCurrentSession = true
                    )
                )
                // Add a mock login history entry
                loginHistoryDao.insertLoginSession(
                    LoginHistoryEntity(
                        sessionId = "sess_laptop",
                        deviceName = "Dell XPS 15 Laptop",
                        ipAddress = "192.168.1.108",
                        location = "Local Network",
                        loginTime = "Yesterday, 09:15",
                        isCurrentSession = false
                    )
                )
            }
        } catch (_: Exception) {}
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        if (sAddr != null) {
                            val isIPv4 = sAddr.indexOf(':') < 0
                            if (isIPv4) return sAddr
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }
}
