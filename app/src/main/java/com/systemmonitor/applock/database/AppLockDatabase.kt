package com.systemmonitor.applock.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LockedAppEntity::class,
        LockSettingsEntity::class,
        UnlockHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppLockDatabase : RoomDatabase() {
    abstract fun appLockDao(): AppLockDao
}
