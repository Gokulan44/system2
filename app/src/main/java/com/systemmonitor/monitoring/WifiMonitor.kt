package com.systemmonitor.monitoring

import android.content.Context
import android.net.wifi.WifiManager
import com.systemmonitor.core.AppLogger
import com.systemmonitor.domain.model.WifiInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SSID/BSSID require ACCESS_FINE_LOCATION on API < 33 (and nearby-devices
 * permission behavior varies by OEM) — without it Android returns
 * "<unknown ssid>" rather than throwing, which we normalize to null here.
 * We never request location permission just to read Wi-Fi name; if it's
 * missing we simply show signal strength without SSID.
 */
@Singleton
class WifiMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun readCurrent(): WifiInfo? {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null

        val info = try {
            @Suppress("DEPRECATION")
            wifiManager.connectionInfo
        } catch (e: SecurityException) {
            AppLogger.w("WifiMonitor", "Missing permission to read Wi-Fi info", e)
            null
        } ?: return null

        val rawSsid = info.ssid?.trim('"')
        val ssid = rawSsid?.takeUnless { it == "<unknown ssid>" || it.isBlank() }

        return WifiInfo(
            timestamp = System.currentTimeMillis(),
            ssid = ssid,
            bssid = info.bssid?.takeUnless { it == "02:00:00:00:00:00" },
            rssiDbm = info.rssi,
            linkSpeedMbps = info.linkSpeed,
            frequencyMhz = info.frequency
        )
    }
}
