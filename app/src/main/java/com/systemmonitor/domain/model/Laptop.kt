package com.systemmonitor.domain.model

enum class ConnectionMode {
    LOCAL,   // Direct HTTP to 192.168.x.x:8765 (same router / LAN)
    REMOTE   // Firebase Firestore relay (different network / long distance)
}

data class Laptop(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int = 8765,
    val os: String = "Windows 11 Pro",
    val status: LaptopStatus = LaptopStatus.OFFLINE,
    val isLocalConnection: Boolean = true,
    val connectionMode: ConnectionMode = ConnectionMode.LOCAL,
    val accessToken: String? = null,
    val lastSeen: Long = System.currentTimeMillis(),
    val macAddress: String? = null
)
