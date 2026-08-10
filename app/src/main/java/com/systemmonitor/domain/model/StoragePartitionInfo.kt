package com.systemmonitor.domain.model

data class StoragePartitionInfo(
    val device: String = "C:",
    val mountpoint: String = "C:\\",
    val fstype: String = "NTFS",
    val totalBytes: Long = 0,
    val usedBytes: Long = 0,
    val freeBytes: Long = 0,
    val usagePercent: Double = 0.0
)
