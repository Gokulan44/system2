package com.systemmonitor.domain.model

enum class TransportType { WIFI, CELLULAR, ETHERNET, VPN, NONE }

data class NetworkInfo(
    val timestamp: Long,
    val isConnected: Boolean,
    val transportType: TransportType,
    val isMetered: Boolean,
    val downstreamKbps: Int,
    val upstreamKbps: Int
)

data class WifiInfo(
    val timestamp: Long,
    val ssid: String?,
    val bssid: String?,
    val rssiDbm: Int,
    val linkSpeedMbps: Int,
    val frequencyMhz: Int
) {
    /** Rough 0-4 bar signal strength from RSSI. */
    val signalBars: Int get() = when {
        rssiDbm >= -50 -> 4
        rssiDbm >= -60 -> 3
        rssiDbm >= -70 -> 2
        rssiDbm >= -80 -> 1
        else -> 0
    }
}
