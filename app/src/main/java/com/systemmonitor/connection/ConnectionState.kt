package com.systemmonitor.connection

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val latencyMs: Long, val mode: ConnectionMode) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

enum class ConnectionMode {
    LOCAL_LAN,
    CLOUD_RELAY
}
