package com.systemmonitor.domain.model

enum class ChargePlug { AC, USB, WIRELESS, NONE }

enum class BatteryHealth { GOOD, OVERHEAT, DEAD, OVER_VOLTAGE, COLD, UNKNOWN }

data class Battery(
    val timestamp: Long,
    val levelPercent: Int,
    val isCharging: Boolean,
    val chargePlug: ChargePlug,
    val health: BatteryHealth,
    val temperatureCelsius: Double,
    val voltageMillivolts: Int,
    val technology: String?
) {
    val isLow: Boolean get() = levelPercent <= 20 && !isCharging
    val isCritical: Boolean get() = levelPercent <= 10 && !isCharging
    val isOverheating: Boolean get() = temperatureCelsius >= 40.0
}
