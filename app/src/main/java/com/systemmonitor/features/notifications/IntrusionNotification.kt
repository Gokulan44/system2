package com.systemmonitor.features.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.systemmonitor.MainActivity
import com.systemmonitor.features.intrusion.data.entity.IntrusionEventEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IntrusionNotification @Inject constructor(
    @ApplicationContext private val context: Context,
    private val channelManager: NotificationChannelManager
) {
    fun showIntrusionNotification(event: IntrusionEventEntity) {
        channelManager.createNotificationChannels()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAVIGATE_TO", "intrusion_center")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            event.eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = "Critical: failed login attempt registered on paired Laptop."

        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_INTRUSION_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Security Intrusion Detected")
            .setContentText(text)
            .setSubText("Laptop ID: ${event.laptopId}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "VIEW TIMELINE",
                pendingIntent
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(event.eventId.hashCode(), notification)
    }
}
