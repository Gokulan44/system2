package com.systemmonitor.connection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectionManager(
    private val localConnection: LocalConnection,
    private val relayConnection: RelayConnection,
    private val heartbeatManager: HeartbeatManager
) {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    suspend fun connect(targetIp: String, port: Int, deviceId: str = "") {
        _connectionState.value = ConnectionState.Connecting
        val localSuccess = localConnection.connect(targetIp, port)
        if (localSuccess) {
            _connectionState.value = ConnectionState.Connected(latencyMs = 15, mode = ConnectionMode.LOCAL_LAN)
            heartbeatManager.start()
        } else {
            val relaySuccess = relayConnection.connect(deviceId)
            if (relaySuccess) {
                _connectionState.value = ConnectionState.Connected(latencyMs = 120, mode = ConnectionMode.CLOUD_RELAY)
                heartbeatManager.start()
            } else {
                _connectionState.value = ConnectionState.Error("Unable to establish LAN or Relay connection")
            }
        }
    }

    suspend fun disconnect() {
        heartbeatManager.stop()
        localConnection.disconnect()
        relayConnection.disconnect()
        _connectionState.value = ConnectionState.Disconnected
    }
}
