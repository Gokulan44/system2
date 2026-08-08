package com.systemmonitor.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Raw, high-frequency battery reading captured locally.
 * NOT synced row-by-row to Firestore — see BatterySummaryEntity /
 * BatteryMonitor for the rollup that actually leaves the device.
 */
@Entity(tableName = "battery_readings")
data class BatteryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val levelPercent: Int,
    val isCharging: Boolean,
    val chargePlug: String,       // "AC", "USB", "WIRELESS", "NONE"
    val healthStatus: String,     // "GOOD", "OVERHEAT", "DEAD", "OVER_VOLTAGE", "UNKNOWN"
    val temperatureCelsius: Double,
    val voltageMillivolts: Int,
    val technology: String?
)
