package com.systemmonitor.features.settings.data

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
