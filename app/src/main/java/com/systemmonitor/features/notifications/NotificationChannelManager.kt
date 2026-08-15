package com.systemmonitor.features.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_PERMISSION_ID = "channel_permission_requests"
        const val CHANNEL_DOWNLOAD_ID = "channel_download_results"
        const val CHANNEL_INTRUSION_ID = "channel_intrusion_alerts"
        const val CHANNEL_TELEMETRY_ID = "channel_telemetry_alerts"
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // 1. Permission request channel (High priority)
            val permChannel = NotificationChannel(
                CHANNEL_PERMISSION_ID,
                "Permission Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when your laptop requests access or download permissions"
                enableVibration(true)
            }

            // 2. Download result channel (Default priority)
            val downloadChannel = NotificationChannel(
                CHANNEL_DOWNLOAD_ID,
                "Download Safety Scans",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies you of the results of download scans and quarantines"
            }

            // 3. Intrusion Alerts channel (High priority)
            val intrusionChannel = NotificationChannel(
                CHANNEL_INTRUSION_ID,
                "Intrusion Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when a failed logon or intrusion is registered on your laptop"
                enableVibration(true)
            }

            // 4. Telemetry Alerts channel (Default priority)
            val telemetryChannel = NotificationChannel(
                CHANNEL_TELEMETRY_ID,
                "Device Telemetry & Settings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies you when battery is low, storage is full, or system metrics require attention"
            }

            manager.createNotificationChannel(permChannel)
            manager.createNotificationChannel(downloadChannel)
            manager.createNotificationChannel(intrusionChannel)
            manager.createNotificationChannel(telemetryChannel)
        }
    }
}
