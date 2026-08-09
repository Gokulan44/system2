package com.systemmonitor.features.settings.data

data class PrivacySettingsEntity(
    val appPermissionsGrantedCount: Int = 14,
    val usageAccessEnabled: Boolean = true,
    val notificationAccessEnabled: Boolean = true,
    val accessibilityEnabled: Boolean = false,
    val deviceAdminActive: Boolean = true,
    val dataCollectionConsent: Boolean = false
)
