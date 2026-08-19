package com.systemmonitor.notification

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.systemmonitor.notification.data.NotificationDao
import com.systemmonitor.notification.data.NotificationEntity
import com.systemmonitor.notification.event.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemMonitorNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao,
    private val permissionManager: NotificationPermissionManager,
    private val clickHandler: NotificationClickHandler
) {
    private val androidNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    NotificationChannels.SECURITY_CHANNEL_ID,
                    NotificationChannels.SECURITY_CHANNEL_NAME,
                    AndroidNotificationManager.IMPORTANCE_HIGH
                ).apply { description = NotificationChannels.SECURITY_CHANNEL_DESC },
                NotificationChannel(
                    NotificationChannels.DEVICE_CHANNEL_ID,
                    NotificationChannels.DEVICE_CHANNEL_NAME,
                    AndroidNotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = NotificationChannels.DEVICE_CHANNEL_DESC },
                NotificationChannel(
                    NotificationChannels.VAULT_CHANNEL_ID,
                    NotificationChannels.VAULT_CHANNEL_NAME,
                    AndroidNotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = NotificationChannels.VAULT_CHANNEL_DESC },
                NotificationChannel(
                    NotificationChannels.APPLOCK_CHANNEL_ID,
                    NotificationChannels.APPLOCK_CHANNEL_NAME,
                    AndroidNotificationManager.IMPORTANCE_HIGH
                ).apply { description = NotificationChannels.APPLOCK_CHANNEL_DESC },
                NotificationChannel(
                    NotificationChannels.NETWORK_CHANNEL_ID,
                    NotificationChannels.NETWORK_CHANNEL_NAME,
                    AndroidNotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = NotificationChannels.NETWORK_CHANNEL_DESC }
            )
            androidNotificationManager.createNotificationChannels(channels)
        }
    }

    fun triggerSecurityNotification(event: SecurityNotificationEvent) {
        postNotification(
            title = event.title,
            message = event.message,
            type = NotificationType.SECURITY,
            channelId = NotificationChannels.SECURITY_CHANNEL_ID,
            priority = event.priority,
            actionData = "security"
        )
    }

    fun triggerDeviceNotification(event: DeviceNotificationEvent) {
        postNotification(
            title = event.title,
            message = event.message,
            type = NotificationType.DEVICE,
            channelId = NotificationChannels.DEVICE_CHANNEL_ID,
            priority = event.priority,
            actionData = "devices"
        )
    }

    fun triggerVaultNotification(event: VaultNotificationEvent) {
        postNotification(
            title = event.title,
            message = event.message,
            type = NotificationType.VAULT,
            channelId = NotificationChannels.VAULT_CHANNEL_ID,
            priority = event.priority,
            actionData = "vault"
        )
    }

    fun triggerAppLockNotification(event: AppLockNotificationEvent) {
        postNotification(
            title = event.title,
            message = event.message,
            type = NotificationType.APPLOCK,
            channelId = NotificationChannels.APPLOCK_CHANNEL_ID,
            priority = event.priority,
            actionData = "applock"
        )
    }

    fun triggerNetworkNotification(event: NetworkNotificationEvent) {
        postNotification(
            title = event.title,
            message = event.message,
            type = NotificationType.NETWORK,
            channelId = NotificationChannels.NETWORK_CHANNEL_ID,
            priority = event.priority,
            actionData = "dashboard"
        )
    }

    private fun postNotification(
        title: String,
        message: String,
        type: NotificationType,
        channelId: String,
        priority: NotificationPriority,
        actionData: String?
    ) {
        val id = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // Save to local database
        scope.launch {
            notificationDao.insertNotification(
                NotificationEntity(
                    id = id,
                    title = title,
                    message = message,
                    type = type.name,
                    timestamp = timestamp,
                    actionData = actionData,
                    priority = priority.name
                )
            )
        }

        // Post system notification if permissions granted
        if (permissionManager.hasNotificationPermission()) {
            val intent = clickHandler.handleNotificationClick(context, actionData)
            val pendingIntent = PendingIntent.getActivity(
                context,
                timestamp.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon
                .setPriority(
                    when (priority) {
                        NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                        NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
                        else -> NotificationCompat.PRIORITY_DEFAULT
                    }
                )
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            androidNotificationManager.notify(timestamp.toInt(), builder.build())
        }
    }
}
