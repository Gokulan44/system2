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
