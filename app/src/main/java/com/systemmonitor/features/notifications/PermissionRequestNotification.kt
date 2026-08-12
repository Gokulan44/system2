package com.systemmonitor.features.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.systemmonitor.MainActivity
import com.systemmonitor.features.remotepermission.domain.model.PermissionRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DecimalFormat
import javax.inject.Inject

class PermissionRequestNotification @Inject constructor(
    @ApplicationContext private val context: Context,
    private val channelManager: NotificationChannelManager
) {
    fun showNotification(request: PermissionRequest) {
        channelManager.createNotificationChannels()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAVIGATE_TO", "resource_permission")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            request.requestId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sizeMb = DecimalFormat("#.##").format(request.resource.sizeBytes.toDouble() / (1024 * 1024))
        
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_PERMISSION_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("🔐 Permission Request")
            .setContentText("Your laptop wants to download: ${request.resource.name} ($sizeMb MB)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "REVIEW REQUEST",
                pendingIntent
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(request.requestId.hashCode(), notification)
    }
}
