package com.systemmonitor.domain.model

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
