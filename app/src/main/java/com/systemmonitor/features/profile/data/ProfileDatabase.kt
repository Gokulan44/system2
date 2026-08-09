package com.systemmonitor.features.profile.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.systemmonitor.features.profile.data.dao.*
import com.systemmonitor.features.profile.data.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        LoginHistoryEntity::class,
        ActivityHistoryEntity::class,
        DeviceSessionEntity::class,
        NotificationPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ProfileDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun loginHistoryDao(): LoginHistoryDao
    abstract fun activityHistoryDao(): ActivityHistoryDao
    abstract fun deviceSessionDao(): DeviceSessionDao
    abstract fun notificationPreferenceDao(): NotificationPreferenceDao
}
