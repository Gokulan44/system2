package com.systemmonitor.features.settings

import android.content.Context
import android.content.SharedPreferences
import com.systemmonitor.features.settings.data.MonitoringSettingsEntity
import com.systemmonitor.features.settings.data.NotificationSettingsEntity
import com.systemmonitor.features.settings.data.PowerSettingsEntity
import com.systemmonitor.features.settings.data.PrivacySettingsEntity
import com.systemmonitor.features.settings.data.RemoteSettingsEntity
import com.systemmonitor.features.settings.data.ScreenSettingsEntity
import com.systemmonitor.features.settings.data.SecuritySettingsEntity
import com.systemmonitor.features.settings.data.SettingsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Settings repository with REAL persistence.
 *
 * Previously this was memory-only: every Settings UI change was lost the moment
 * the process died. All 7 sub-entities are now persisted to SharedPreferences
 * and rehydrated on process start.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("system_monitor_settings_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<SettingsEntity> = _settingsFlow.asStateFlow()

    fun updateSettings(newSettings: SettingsEntity) {
        _settingsFlow.value = newSettings
        persistSettings(newSettings)
    }

    // ------------------------------------------------------------------ persist

    private fun persistSettings(settings: SettingsEntity) {
        prefs.edit().apply {
            // Security
            putBoolean(K_SEC_DEVICE, settings.security.deviceSecurityEnabled)
            putBoolean(K_SEC_APPLOCK, settings.security.appLockEnabled)
            putString(K_SEC_LOCK_METHOD, settings.security.lockMethod)
            putBoolean(K_SEC_BIOMETRIC, settings.security.biometricEnabled)
            putString(K_SEC_AUTOLOCK_TIMING, settings.security.autoLockTiming)
            putBoolean(K_SEC_VIRUS_SCAN, settings.security.virusScanEnabled)
            putBoolean(K_SEC_PRIVACY_CHECK, settings.security.privacyCheckEnabled)
            putBoolean(K_SEC_ALERTS, settings.security.securityAlertsEnabled)
            // Notifications
            putBoolean(K_NOTIF_MASTER, settings.notifications.masterNotificationsEnabled)
            putBoolean(K_NOTIF_SECURITY, settings.notifications.securityAlerts)
            putBoolean(K_NOTIF_DEVICE, settings.notifications.deviceAlerts)
            putBoolean(K_NOTIF_BATTERY, settings.notifications.batteryAlerts)
            putBoolean(K_NOTIF_STORAGE, settings.notifications.storageAlerts)
            putBoolean(K_NOTIF_NETWORK, settings.notifications.networkAlerts)
            putBoolean(K_NOTIF_APP_USAGE, settings.notifications.appUsageAlerts)
            putBoolean(K_NOTIF_REPORTS, settings.notifications.reportNotifications)
            // Power
            putBoolean(K_POWER_BATTERY_MON, settings.power.batteryMonitoringEnabled)
            putInt(K_POWER_BATTERY_THRESHOLD, settings.power.batteryAlertThreshold)
            putBoolean(K_POWER_SAVING_AUTO, settings.power.powerSavingAutoActivate)
            putBoolean(K_POWER_SLEEP_CONF, settings.power.remoteSleepConfirmation)
            putBoolean(K_POWER_RESTART_CONF, settings.power.remoteRestartConfirmation)
            putBoolean(K_POWER_SHUTDOWN_CONF, settings.power.remoteShutdownConfirmation)
            putBoolean(K_POWER_BG_SCREEN_OFF, settings.power.backgroundMonitoringScreenOff)
            // Screen
            putInt(K_SCREEN_BRIGHTNESS, settings.screen.brightnessPercent)
            putBoolean(K_SCREEN_AUTO_BRIGHTNESS, settings.screen.autoBrightnessEnabled)
            putBoolean(K_SCREEN_DARK_MODE, settings.screen.darkModeEnabled)
            putInt(K_SCREEN_TIMEOUT, settings.screen.screenTimeoutSeconds)
            putBoolean(K_SCREEN_REMOTE_FULLSCREEN, settings.screen.remoteScreenFullScreen)
            // Monitoring
            putBoolean(K_MON_REALTIME, settings.monitoring.realTimeMonitoringEnabled)
            putInt(K_MON_REFRESH_RATE, settings.monitoring.refreshRateSeconds)
            putBoolean(K_MON_CPU, settings.monitoring.cpuMonitoringEnabled)
            putInt(K_MON_CPU_THRESHOLD, settings.monitoring.cpuWarningThreshold)
            putBoolean(K_MON_RAM, settings.monitoring.ramMonitoringEnabled)
            putInt(K_MON_RAM_THRESHOLD, settings.monitoring.ramWarningThreshold)
            putBoolean(K_MON_STORAGE, settings.monitoring.storageMonitoringEnabled)
            putBoolean(K_MON_NETWORK, settings.monitoring.networkMonitoringEnabled)
            putBoolean(K_MON_PROCESS, settings.monitoring.processMonitoringEnabled)
            // Privacy
            putInt(K_PRIV_PERMISSIONS_GRANTED, settings.privacy.appPermissionsGrantedCount)
            putBoolean(K_PRIV_USAGE_ACCESS, settings.privacy.usageAccessEnabled)
            putBoolean(K_PRIV_NOTIF_ACCESS, settings.privacy.notificationAccessEnabled)
            putBoolean(K_PRIV_ACCESSIBILITY, settings.privacy.accessibilityEnabled)
            putBoolean(K_PRIV_DEVICE_ADMIN, settings.privacy.deviceAdminActive)
            putBoolean(K_PRIV_DATA_COLLECTION, settings.privacy.dataCollectionConsent)
            // Remote
            putBoolean(K_REMOTE_CONNECTED, settings.remote.laptopConnected)
            putString(K_REMOTE_LAPTOP_NAME, settings.remote.laptopName)
            putBoolean(K_REMOTE_SCREEN, settings.remote.remoteScreenEnabled)
            putBoolean(K_REMOTE_CONTROL, settings.remote.remoteControlEnabled)
            putString(K_REMOTE_PAIRING_CODE, settings.remote.pairingCode)
            putInt(K_REMOTE_TRUSTED_DEVICES, settings.remote.trustedDeviceCount)
        }.apply()
    }

    // ------------------------------------------------------------------ hydrate

    private fun loadSettings(): SettingsEntity {
        val defaults = SettingsEntity()
        return SettingsEntity(
            security = SecuritySettingsEntity(
                deviceSecurityEnabled = prefs.getBoolean(K_SEC_DEVICE, defaults.security.deviceSecurityEnabled),
                appLockEnabled = prefs.getBoolean(K_SEC_APPLOCK, defaults.security.appLockEnabled),
                lockMethod = prefs.getString(K_SEC_LOCK_METHOD, defaults.security.lockMethod) ?: defaults.security.lockMethod,
                biometricEnabled = prefs.getBoolean(K_SEC_BIOMETRIC, defaults.security.biometricEnabled),
                autoLockTiming = prefs.getString(K_SEC_AUTOLOCK_TIMING, defaults.security.autoLockTiming) ?: defaults.security.autoLockTiming,
                virusScanEnabled = prefs.getBoolean(K_SEC_VIRUS_SCAN, defaults.security.virusScanEnabled),
                privacyCheckEnabled = prefs.getBoolean(K_SEC_PRIVACY_CHECK, defaults.security.privacyCheckEnabled),
                securityAlertsEnabled = prefs.getBoolean(K_SEC_ALERTS, defaults.security.securityAlertsEnabled)
            ),
            notifications = NotificationSettingsEntity(
                masterNotificationsEnabled = prefs.getBoolean(K_NOTIF_MASTER, defaults.notifications.masterNotificationsEnabled),
                securityAlerts = prefs.getBoolean(K_NOTIF_SECURITY, defaults.notifications.securityAlerts),
                deviceAlerts = prefs.getBoolean(K_NOTIF_DEVICE, defaults.notifications.deviceAlerts),
                batteryAlerts = prefs.getBoolean(K_NOTIF_BATTERY, defaults.notifications.batteryAlerts),
                storageAlerts = prefs.getBoolean(K_NOTIF_STORAGE, defaults.notifications.storageAlerts),
                networkAlerts = prefs.getBoolean(K_NOTIF_NETWORK, defaults.notifications.networkAlerts),
                appUsageAlerts = prefs.getBoolean(K_NOTIF_APP_USAGE, defaults.notifications.appUsageAlerts),
                reportNotifications = prefs.getBoolean(K_NOTIF_REPORTS, defaults.notifications.reportNotifications)
            ),
            power = PowerSettingsEntity(
                batteryMonitoringEnabled = prefs.getBoolean(K_POWER_BATTERY_MON, defaults.power.batteryMonitoringEnabled),
                batteryAlertThreshold = prefs.getInt(K_POWER_BATTERY_THRESHOLD, defaults.power.batteryAlertThreshold),
                powerSavingAutoActivate = prefs.getBoolean(K_POWER_SAVING_AUTO, defaults.power.powerSavingAutoActivate),
                remoteSleepConfirmation = prefs.getBoolean(K_POWER_SLEEP_CONF, defaults.power.remoteSleepConfirmation),
                remoteRestartConfirmation = prefs.getBoolean(K_POWER_RESTART_CONF, defaults.power.remoteRestartConfirmation),
                remoteShutdownConfirmation = prefs.getBoolean(K_POWER_SHUTDOWN_CONF, defaults.power.remoteShutdownConfirmation),
                backgroundMonitoringScreenOff = prefs.getBoolean(K_POWER_BG_SCREEN_OFF, defaults.power.backgroundMonitoringScreenOff)
            ),
            screen = ScreenSettingsEntity(
                brightnessPercent = prefs.getInt(K_SCREEN_BRIGHTNESS, defaults.screen.brightnessPercent),
                autoBrightnessEnabled = prefs.getBoolean(K_SCREEN_AUTO_BRIGHTNESS, defaults.screen.autoBrightnessEnabled),
                darkModeEnabled = prefs.getBoolean(K_SCREEN_DARK_MODE, defaults.screen.darkModeEnabled),
                screenTimeoutSeconds = prefs.getInt(K_SCREEN_TIMEOUT, defaults.screen.screenTimeoutSeconds),
                remoteScreenFullScreen = prefs.getBoolean(K_SCREEN_REMOTE_FULLSCREEN, defaults.screen.remoteScreenFullScreen)
            ),
            monitoring = MonitoringSettingsEntity(
                realTimeMonitoringEnabled = prefs.getBoolean(K_MON_REALTIME, defaults.monitoring.realTimeMonitoringEnabled),
                refreshRateSeconds = prefs.getInt(K_MON_REFRESH_RATE, defaults.monitoring.refreshRateSeconds),
                cpuMonitoringEnabled = prefs.getBoolean(K_MON_CPU, defaults.monitoring.cpuMonitoringEnabled),
                cpuWarningThreshold = prefs.getInt(K_MON_CPU_THRESHOLD, defaults.monitoring.cpuWarningThreshold),
                ramMonitoringEnabled = prefs.getBoolean(K_MON_RAM, defaults.monitoring.ramMonitoringEnabled),
                ramWarningThreshold = prefs.getInt(K_MON_RAM_THRESHOLD, defaults.monitoring.ramWarningThreshold),
                storageMonitoringEnabled = prefs.getBoolean(K_MON_STORAGE, defaults.monitoring.storageMonitoringEnabled),
                networkMonitoringEnabled = prefs.getBoolean(K_MON_NETWORK, defaults.monitoring.networkMonitoringEnabled),
                processMonitoringEnabled = prefs.getBoolean(K_MON_PROCESS, defaults.monitoring.processMonitoringEnabled)
            ),
            privacy = PrivacySettingsEntity(
                appPermissionsGrantedCount = prefs.getInt(K_PRIV_PERMISSIONS_GRANTED, defaults.privacy.appPermissionsGrantedCount),
                usageAccessEnabled = prefs.getBoolean(K_PRIV_USAGE_ACCESS, defaults.privacy.usageAccessEnabled),
                notificationAccessEnabled = prefs.getBoolean(K_PRIV_NOTIF_ACCESS, defaults.privacy.notificationAccessEnabled),
                accessibilityEnabled = prefs.getBoolean(K_PRIV_ACCESSIBILITY, defaults.privacy.accessibilityEnabled),
                deviceAdminActive = prefs.getBoolean(K_PRIV_DEVICE_ADMIN, defaults.privacy.deviceAdminActive),
                dataCollectionConsent = prefs.getBoolean(K_PRIV_DATA_COLLECTION, defaults.privacy.dataCollectionConsent)
            ),
            remote = RemoteSettingsEntity(
                laptopConnected = prefs.getBoolean(K_REMOTE_CONNECTED, defaults.remote.laptopConnected),
                laptopName = prefs.getString(K_REMOTE_LAPTOP_NAME, defaults.remote.laptopName) ?: defaults.remote.laptopName,
                remoteScreenEnabled = prefs.getBoolean(K_REMOTE_SCREEN, defaults.remote.remoteScreenEnabled),
                remoteControlEnabled = prefs.getBoolean(K_REMOTE_CONTROL, defaults.remote.remoteControlEnabled),
                pairingCode = prefs.getString(K_REMOTE_PAIRING_CODE, defaults.remote.pairingCode) ?: defaults.remote.pairingCode,
                trustedDeviceCount = prefs.getInt(K_REMOTE_TRUSTED_DEVICES, defaults.remote.trustedDeviceCount)
            )
        )
    }

    // ------------------------------------------------------------------ keys

    private companion object {
        const val K_SEC_DEVICE = "sec_device_security_enabled"
        const val K_SEC_APPLOCK = "sec_app_lock_enabled"
        const val K_SEC_LOCK_METHOD = "sec_lock_method"
        const val K_SEC_BIOMETRIC = "sec_biometric_enabled"
        const val K_SEC_AUTOLOCK_TIMING = "sec_auto_lock_timing"
        const val K_SEC_VIRUS_SCAN = "sec_virus_scan_enabled"
        const val K_SEC_PRIVACY_CHECK = "sec_privacy_check_enabled"
        const val K_SEC_ALERTS = "sec_security_alerts_enabled"

        const val K_NOTIF_MASTER = "notif_master_enabled"
        const val K_NOTIF_SECURITY = "notif_security_alerts"
        const val K_NOTIF_DEVICE = "notif_device_alerts"
        const val K_NOTIF_BATTERY = "notif_battery_alerts"
        const val K_NOTIF_STORAGE = "notif_storage_alerts"
        const val K_NOTIF_NETWORK = "notif_network_alerts"
        const val K_NOTIF_APP_USAGE = "notif_app_usage_alerts"
        const val K_NOTIF_REPORTS = "notif_report_notifications"

        const val K_POWER_BATTERY_MON = "power_battery_monitoring_enabled"
        const val K_POWER_BATTERY_THRESHOLD = "power_battery_alert_threshold"
        const val K_POWER_SAVING_AUTO = "power_saving_auto_activate"
        const val K_POWER_SLEEP_CONF = "power_remote_sleep_confirmation"
        const val K_POWER_RESTART_CONF = "power_remote_restart_confirmation"
        const val K_POWER_SHUTDOWN_CONF = "power_remote_shutdown_confirmation"
        const val K_POWER_BG_SCREEN_OFF = "power_background_monitoring_screen_off"

        const val K_SCREEN_BRIGHTNESS = "screen_brightness_percent"
        const val K_SCREEN_AUTO_BRIGHTNESS = "screen_auto_brightness_enabled"
        const val K_SCREEN_DARK_MODE = "screen_dark_mode_enabled"
        const val K_SCREEN_TIMEOUT = "screen_timeout_seconds"
        const val K_SCREEN_REMOTE_FULLSCREEN = "screen_remote_full_screen"

        const val K_MON_REALTIME = "mon_real_time_monitoring_enabled"
        const val K_MON_REFRESH_RATE = "mon_refresh_rate_seconds"
        const val K_MON_CPU = "mon_cpu_monitoring_enabled"
        const val K_MON_CPU_THRESHOLD = "mon_cpu_warning_threshold"
        const val K_MON_RAM = "mon_ram_monitoring_enabled"
        const val K_MON_RAM_THRESHOLD = "mon_ram_warning_threshold"
        const val K_MON_STORAGE = "mon_storage_monitoring_enabled"
        const val K_MON_NETWORK = "mon_network_monitoring_enabled"
        const val K_MON_PROCESS = "mon_process_monitoring_enabled"

        const val K_PRIV_PERMISSIONS_GRANTED = "priv_app_permissions_granted_count"
        const val K_PRIV_USAGE_ACCESS = "priv_usage_access_enabled"
        const val K_PRIV_NOTIF_ACCESS = "priv_notification_access_enabled"
        const val K_PRIV_ACCESSIBILITY = "priv_accessibility_enabled"
        const val K_PRIV_DEVICE_ADMIN = "priv_device_admin_active"
        const val K_PRIV_DATA_COLLECTION = "priv_data_collection_consent"

        const val K_REMOTE_CONNECTED = "remote_laptop_connected"
        const val K_REMOTE_LAPTOP_NAME = "remote_laptop_name"
        const val K_REMOTE_SCREEN = "remote_screen_enabled"
        const val K_REMOTE_CONTROL = "remote_control_enabled"
        const val K_REMOTE_PAIRING_CODE = "remote_pairing_code"
        const val K_REMOTE_TRUSTED_DEVICES = "remote_trusted_device_count"
    }
}