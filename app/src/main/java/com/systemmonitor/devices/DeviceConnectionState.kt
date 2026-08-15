package com.systemmonitor.devices

sealed class DeviceConnectionState {
    object Disconnected : DeviceConnectionState()
    object Connecting : DeviceConnectionState()
    data class Connected(val deviceId: String, val latencyMs: Long) : DeviceConnectionState()
    data class Error(val message: String) : DeviceConnectionState()
}
