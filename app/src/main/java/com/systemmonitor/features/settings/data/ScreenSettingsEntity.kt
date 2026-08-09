package com.systemmonitor.features.settings.data

data class ScreenSettingsEntity(
    val brightnessPercent: Int = 80,
    val autoBrightnessEnabled: Boolean = true,
    val darkModeEnabled: Boolean = true,
    val screenTimeoutSeconds: Int = 60,
    val remoteScreenFullScreen: Boolean = false
)
