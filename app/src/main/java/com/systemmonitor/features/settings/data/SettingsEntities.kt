package com.systemmonitor.features.settings.data

data class SecuritySettingsEntity(
    val deviceSecurityEnabled: Boolean = true,
    val appLockEnabled: Boolean = true,
    val lockMethod: String = "PIN",
    val biometricEnabled: Boolean = true,
    val autoLockTiming: String = "Immediately",
    val virusScanEnabled: Boolean = true,
    val privacyCheckEnabled: Boolean = true,
    val securityAlertsEnabled: Boolean = true
)

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

data class PowerSettingsEntity(
    val batteryMonitoringEnabled: Boolean = true,
    val batteryAlertThreshold: Int = 20,
    val powerSavingAutoActivate: Boolean = true,
    val remoteSleepConfirmation: Boolean = true,
    val remoteRestartConfirmation: Boolean = true,
    val remoteShutdownConfirmation: Boolean = true,
    val backgroundMonitoringScreenOff: Boolean = true
)

data class ScreenSettingsEntity(
    val brightnessPercent: Int = 80,
    val autoBrightnessEnabled: Boolean = true,
    val darkModeEnabled: Boolean = true,
    val screenTimeoutSeconds: Int = 60,
    val remoteScreenFullScreen: Boolean = false
)

data class MonitoringSettingsEntity(
    val realTimeMonitoringEnabled: Boolean = true,
    val refreshRateSeconds: Int = 2,
    val cpuMonitoringEnabled: Boolean = true,
    val cpuWarningThreshold: Int = 80,
    val ramMonitoringEnabled: Boolean = true,
    val ramWarningThreshold: Int = 85,
    val storageMonitoringEnabled: Boolean = true,
    val networkMonitoringEnabled: Boolean = true,
    val processMonitoringEnabled: Boolean = true
)

data class PrivacySettingsEntity(
    val appPermissionsGrantedCount: Int = 14,
    val usageAccessEnabled: Boolean = true,
    val notificationAccessEnabled: Boolean = true,
    val accessibilityEnabled: Boolean = false,
    val deviceAdminActive: Boolean = true,
    val dataCollectionConsent: Boolean = false
)

data class RemoteSettingsEntity(
    val laptopConnected: Boolean = true,
    val laptopName: String = "Windows-Workstation-Pro",
    val remoteScreenEnabled: Boolean = true,
    val remoteControlEnabled: Boolean = true,
    val pairingCode: String = "948201",
    val trustedDeviceCount: Int = 2
)

data class SettingsEntity(
    val security: SecuritySettingsEntity = SecuritySettingsEntity(),
    val notifications: NotificationSettingsEntity = NotificationSettingsEntity(),
    val power: PowerSettingsEntity = PowerSettingsEntity(),
    val screen: ScreenSettingsEntity = ScreenSettingsEntity(),
    val monitoring: MonitoringSettingsEntity = MonitoringSettingsEntity(),
    val privacy: PrivacySettingsEntity = PrivacySettingsEntity(),
    val remote: RemoteSettingsEntity = RemoteSettingsEntity()
)
