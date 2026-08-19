package com.systemmonitor.notification.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM system_notifications_table ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM system_notifications_table WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE system_notifications_table SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE system_notifications_table SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM system_notifications_table WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM system_notifications_table")
    suspend fun clearAllNotifications()
}
