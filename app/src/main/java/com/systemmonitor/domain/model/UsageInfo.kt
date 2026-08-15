package com.systemmonitor.domain.model

data class UsageInfo(
    val cpu: CpuInfo = CpuInfo(),
    val memory: MemoryInfo = MemoryInfo(),
    val storage: StorageInfo = StorageInfo(),
    val battery: LaptopBatteryInfo = LaptopBatteryInfo(),
    val network: LaptopNetworkInfo = LaptopNetworkInfo(),
    val uptimeSeconds: Double = 0.0,
    val isLocked: Boolean = true
)
