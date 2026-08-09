package com.systemmonitor.features.settings.data

data class PowerSettingsEntity(
    val batteryMonitoringEnabled: Boolean = true,
    val batteryAlertThreshold: Int = 20,
    val powerSavingAutoActivate: Boolean = true,
    val remoteSleepConfirmation: Boolean = true,
    val remoteRestartConfirmation: Boolean = true,
    val remoteShutdownConfirmation: Boolean = true,
    val backgroundMonitoringScreenOff: Boolean = true
)
