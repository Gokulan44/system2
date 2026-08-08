package com.systemmonitor.monitoring

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.systemmonitor.domain.model.Battery
import com.systemmonitor.domain.model.BatteryHealth
import com.systemmonitor.domain.model.ChargePlug
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads the current battery state directly from the OS.
 * Uses the sticky ACTION_BATTERY_CHANGED intent, which requires no permission
 * and doesn't need a registered receiver to read once.
 */
@Singleton
class BatteryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun readCurrent(): Battery? {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ) ?: return null

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return null
        val levelPercent = (level * 100) / scale

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargePlug = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> ChargePlug.AC
            BatteryManager.BATTERY_PLUGGED_USB -> ChargePlug.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargePlug.WIRELESS
            else -> ChargePlug.NONE
        }

        val healthExtra = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val health = when (healthExtra) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.COLD
            else -> BatteryHealth.UNKNOWN
        }

        val tempTenthsCelsius = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

        return Battery(
            timestamp = System.currentTimeMillis(),
            levelPercent = levelPercent,
            isCharging = isCharging,
            chargePlug = chargePlug,
            health = health,
            temperatureCelsius = tempTenthsCelsius / 10.0,
            voltageMillivolts = voltage,
            technology = technology
        )
    }
}
