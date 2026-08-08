package com.systemmonitor.domain.model

data class Memory(
    val timestamp: Long,
    val totalMb: Long,
    val availableMb: Long,
    val usedMb: Long,
    val thresholdMb: Long,
    val isLowMemory: Boolean
) {
    val usedPercent: Int get() = if (totalMb == 0L) 0 else ((usedMb * 100) / totalMb).toInt()
}
