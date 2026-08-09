package com.systemmonitor.features.settings.data

data class RemoteSettingsEntity(
    val laptopConnected: Boolean = true,
    val laptopName: String = "Windows-Workstation-Pro",
    val remoteScreenEnabled: Boolean = true,
    val remoteControlEnabled: Boolean = true,
    val pairingCode: String = "948201",
    val trustedDeviceCount: Int = 2
)
