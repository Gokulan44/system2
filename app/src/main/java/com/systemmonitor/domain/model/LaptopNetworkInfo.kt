package com.systemmonitor.domain.model

data class LaptopNetworkInfo(
    val hostname: String = "DESKTOP-LAPTOP",
    val primaryIp: String = "192.168.1.50",
    val bytesSent: Long = 0,
    val bytesRecv: Long = 0,
    val packetsSent: Long = 0,
    val packetsRecv: Long = 0,
    val interfaces: List<NetworkInterfaceInfo> = emptyList()
)
