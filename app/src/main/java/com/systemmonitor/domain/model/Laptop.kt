package com.systemmonitor.domain.model

enum class LaptopStatus {
    ONLINE,
    OFFLINE,
    PAIRING,
    SLEEPING
}

data class Laptop(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int = 8765,
    val os: String = "Windows 11 Pro",
    val status: LaptopStatus = LaptopStatus.OFFLINE,
    val isLocalConnection: Boolean = true,
    val accessToken: String? = null,
    val lastSeen: Long = System.currentTimeMillis()
)
