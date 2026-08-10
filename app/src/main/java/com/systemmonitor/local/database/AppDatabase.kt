package com.systemmonitor.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.systemmonitor.local.database.dao.BatteryDao
import com.systemmonitor.local.database.dao.InstalledAppDao
import com.systemmonitor.local.database.dao.MemoryDao
import com.systemmonitor.local.database.dao.NetworkDao
import com.systemmonitor.local.database.dao.StorageDao
import com.systemmonitor.local.database.dao.WifiDao
import com.systemmonitor.local.database.entity.BatteryEntity
import com.systemmonitor.local.database.entity.InstalledAppEntity
import com.systemmonitor.local.database.entity.MemoryEntity
import com.systemmonitor.local.database.entity.NetworkEntity
import com.systemmonitor.local.database.entity.StorageEntity
import com.systemmonitor.local.database.entity.WifiEntity

import com.systemmonitor.local.database.dao.LaptopDao
import com.systemmonitor.local.database.entity.LaptopEntity

/**
 * Add new @Entity classes to `entities` and a matching `abstract fun xDao()`
 * as each vertical slice (Cpu, AppUsage, ...) is built out.
 * Bump `version` and add a Migration in DatabaseMigrations.kt for any schema change.
 */
@Database(
    entities = [
        BatteryEntity::class,
        MemoryEntity::class,
        StorageEntity::class,
        InstalledAppEntity::class,
        NetworkEntity::class,
        WifiEntity::class,
        LaptopEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao
    abstract fun memoryDao(): MemoryDao
    abstract fun storageDao(): StorageDao
    abstract fun installedAppDao(): InstalledAppDao
    abstract fun networkDao(): NetworkDao
    abstract fun wifiDao(): WifiDao
    abstract fun laptopDao(): LaptopDao
}
