package com.systemmonitor.domain.model

data class Storage(
    val timestamp: Long,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long
) {
    val usedPercent: Int get() = if (totalBytes == 0L) 0 else ((usedBytes * 100) / totalBytes).toInt()
    val totalGb: Double get() = totalBytes / (1024.0 * 1024.0 * 1024.0)
    val freeGb: Double get() = freeBytes / (1024.0 * 1024.0 * 1024.0)
    val isLowStorage: Boolean get() = usedPercent >= 90
}
