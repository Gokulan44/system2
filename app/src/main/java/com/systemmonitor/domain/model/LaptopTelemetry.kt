package com.systemmonitor.domain.model

data class CpuInfo(
    val processorName: String = "Intel Core i7-13700H",
    val usagePercent: Double = 0.0,
    val logicalCores: Int = 16,
    val physicalCores: Int = 14,
    val frequencyMhz: Double = 3400.0,
    val maxFrequencyMhz: Double = 5000.0,
    val perCpuUsage: List<Double> = emptyList()
)

data class MemoryInfo(
    val totalBytes: Long = 0,
    val availableBytes: Long = 0,
    val usedBytes: Long = 0,
    val freeBytes: Long = 0,
    val usagePercent: Double = 0.0,
    val swapTotalBytes: Long = 0,
    val swapUsedBytes: Long = 0,
    val swapPercent: Double = 0.0
)

data class StoragePartitionInfo(
    val device: String = "C:",
    val mountpoint: String = "C:\\",
    val fstype: String = "NTFS",
    val totalBytes: Long = 0,
    val usedBytes: Long = 0,
    val freeBytes: Long = 0,
    val usagePercent: Double = 0.0
)

data class StorageInfo(
    val overallTotalBytes: Long = 0,
    val overallUsedBytes: Long = 0,
    val overallFreeBytes: Long = 0,
    val overallUsagePercent: Double = 0.0,
    val partitions: List<StoragePartitionInfo> = emptyList()
)

data class LaptopBatteryInfo(
    val hasBattery: Boolean = true,
    val percent: Double = 100.0,
    val powerPlugged: Boolean = true,
    val timeRemainingSeconds: Long = -1,
    val status: String = "AC Power"
)

data class NetworkInterfaceInfo(
    val interfaceName: String = "Wi-Fi",
    val ipAddress: String = "192.168.1.50"
)

data class LaptopNetworkInfo(
    val hostname: String = "DESKTOP-LAPTOP",
    val primaryIp: String = "192.168.1.50",
    val bytesSent: Long = 0,
    val bytesRecv: Long = 0,
    val packetsSent: Long = 0,
    val packetsRecv: Long = 0,
    val interfaces: List<NetworkInterfaceInfo> = emptyList()
)

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val cpuPercent: Double,
    val memoryPercent: Double,
    val status: String,
    val username: String = ""
)

data class UsageInfo(
    val cpu: CpuInfo = CpuInfo(),
    val memory: MemoryInfo = MemoryInfo(),
    val storage: StorageInfo = StorageInfo(),
    val battery: LaptopBatteryInfo = LaptopBatteryInfo(),
    val network: LaptopNetworkInfo = LaptopNetworkInfo(),
    val uptimeSeconds: Double = 0.0
)

enum class CommandType {
    LOCK,
    SLEEP,
    RESTART,
    SHUTDOWN
}

data class RemoteCommand(
    val commandId: String,
    val targetDeviceId: String,
    val type: CommandType,
    val pin: String? = null,
    val delaySeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
