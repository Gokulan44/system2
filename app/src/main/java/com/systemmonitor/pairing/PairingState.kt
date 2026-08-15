package com.systemmonitor.pairing

sealed class PairingState {
    object Idle : PairingState()
    object Scanning : PairingState()
    object Verifying : PairingState()
    data class Paired(val deviceId: String, val deviceName: String) : PairingState()
    data class Error(val message: String) : PairingState()
}
