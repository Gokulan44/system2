package com.systemmonitor.domain.model

data class StorageInfo(
    val overallTotalBytes: Long = 0,
    val overallUsedBytes: Long = 0,
    val overallFreeBytes: Long = 0,
    val overallUsagePercent: Double = 0.0,
    val partitions: List<StoragePartitionInfo> = emptyList()
)
