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

            manager.createNotificationChannel(permChannel)
            manager.createNotificationChannel(downloadChannel)
        }
    }
}
