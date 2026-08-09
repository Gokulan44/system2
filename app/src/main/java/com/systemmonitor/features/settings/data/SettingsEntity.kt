package com.systemmonitor.features.settings.data

data class SettingsEntity(
    val security: SecuritySettingsEntity = SecuritySettingsEntity(),
    val notifications: NotificationSettingsEntity = NotificationSettingsEntity(),
    val power: PowerSettingsEntity = PowerSettingsEntity(),
    val screen: ScreenSettingsEntity = ScreenSettingsEntity(),
    val monitoring: MonitoringSettingsEntity = MonitoringSettingsEntity(),
    val privacy: PrivacySettingsEntity = PrivacySettingsEntity(),
    val remote: RemoteSettingsEntity = RemoteSettingsEntity()
)
