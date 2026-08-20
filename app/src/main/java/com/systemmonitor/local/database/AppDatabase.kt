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
import com.systemmonitor.local.database.dao.UnlockHistoryDao
import com.systemmonitor.local.database.entity.UnlockHistoryEntity
import com.systemmonitor.features.intrusion.data.entity.IntrusionEventEntity
import com.systemmonitor.features.intrusion.data.dao.IntrusionEventDao


@Database(
    entities = [
        BatteryEntity::class,
        MemoryEntity::class,
        StorageEntity::class,
        InstalledAppEntity::class,
        NetworkEntity::class,
        WifiEntity::class,
        LaptopEntity::class,
        UnlockHistoryEntity::class,
        IntrusionEventEntity::class
    ],
    version = 12,
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
    abstract fun unlockHistoryDao(): UnlockHistoryDao
    abstract fun intrusionEventDao(): IntrusionEventDao
}
