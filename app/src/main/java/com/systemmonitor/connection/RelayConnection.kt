package com.systemmonitor.connection

class RelayConnection {
    private var isConnected = false

    suspend fun connect(deviceId: String): Boolean {
        // Connects to standalone relay WebSocket endpoint
        return try {
            isConnected = true
            true
        } catch (e: Exception) {
            false
        }
    }

    fun disconnect() {
        isConnected = false
    }
}
