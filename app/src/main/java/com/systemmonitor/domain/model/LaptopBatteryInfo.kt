package com.systemmonitor.domain.model

data class LaptopBatteryInfo(
    val hasBattery: Boolean = true,
    val percent: Double = 100.0,
    val powerPlugged: Boolean = true,
    val timeRemainingSeconds: Long = -1,
    val status: String = "AC Power"
)
