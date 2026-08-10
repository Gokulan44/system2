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
