package com.systemmonitor.applock.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.systemmonitor.applock.data.entity.AppLockSettingsEntity
import com.systemmonitor.applock.data.entity.AuthenticationLogEntity
import com.systemmonitor.applock.data.entity.LockedAppEntity

/**
 * Spec-tree AppLock database. This class was MISSING — AuthLog/Settings/LockedApp
 * DAOs existed but had no Room instance backing them, so SecurityPolicy and
 * IntrusionLogger could never be constructed at runtime.
 */
@Database(
    entities = [
        LockedAppEntity::class,
        AuthenticationLogEntity::class,
        AppLockSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lockedAppDao(): LockedAppDao
    abstract fun authenticationLogDao(): AuthenticationLogDao
    abstract fun appLockSettingsDao(): AppLockSettingsDao
}