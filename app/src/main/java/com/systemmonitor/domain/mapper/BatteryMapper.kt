package com.systemmonitor.domain.mapper

import com.systemmonitor.domain.model.Battery
import com.systemmonitor.domain.model.BatteryHealth
import com.systemmonitor.domain.model.ChargePlug
import com.systemmonitor.local.database.entity.BatteryEntity

fun BatteryEntity.toDomain(): Battery = Battery(
    timestamp = timestamp,
    levelPercent = levelPercent,
    isCharging = isCharging,
    chargePlug = runCatching { ChargePlug.valueOf(chargePlug) }.getOrDefault(ChargePlug.NONE),
    health = runCatching { BatteryHealth.valueOf(healthStatus) }.getOrDefault(BatteryHealth.UNKNOWN),
    temperatureCelsius = temperatureCelsius,
    voltageMillivolts = voltageMillivolts,
    technology = technology
)

fun Battery.toEntity(): BatteryEntity = BatteryEntity(
    timestamp = timestamp,
    levelPercent = levelPercent,
    isCharging = isCharging,
    chargePlug = chargePlug.name,
    healthStatus = health.name,
    temperatureCelsius = temperatureCelsius,
    voltageMillivolts = voltageMillivolts,
    technology = technology
)
