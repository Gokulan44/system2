package com.systemmonitor.features.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.systemmonitor.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DownloadResultNotification @Inject constructor(
    @ApplicationContext private val context: Context,
    private val channelManager: NotificationChannelManager
) {
    fun showSafeNotification(requestId: String, filename: String, sizeMbText: String, sha256: String) {
        channelManager.createNotificationChannels()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAVIGATE_TO", "resource_center")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_DOWNLOAD_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download Complete")
            .setContentText("✓ $filename ($sizeMbText) downloaded and verified safe.")
            .setSubText("Scan: SAFE")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "VIEW DETAILS",
                pendingIntent
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(requestId.hashCode(), notification)
    }

    fun showQuarantinedNotification(requestId: String, filename: String, reason: String) {
        channelManager.createNotificationChannels()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAVIGATE_TO", "resource_center")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_DOWNLOAD_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Security Alert")
            .setContentText("⚠ $filename quarantined: $reason")
            .setSubText("Scan: SUSPICIOUS")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "VIEW SCAN",
                pendingIntent
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(requestId.hashCode(), notification)
    }
}
