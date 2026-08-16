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
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.database.VaultFolderEntity
import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.database.VaultFolderDao
import com.systemmonitor.vault.database.VaultAuditEntity
import com.systemmonitor.vault.database.VaultAuditDao
import com.systemmonitor.vault.database.VaultSettingsEntity
import com.systemmonitor.vault.database.VaultSettingsDao

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
        IntrusionEventEntity::class,
        VaultFileEntity::class,
        VaultFolderEntity::class,
        VaultAuditEntity::class,
        VaultSettingsEntity::class
    ],
    version = 11,
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
    abstract fun vaultFileDao(): VaultFileDao
    abstract fun vaultFolderDao(): VaultFolderDao
    abstract fun vaultAuditDao(): VaultAuditDao
    abstract fun vaultSettingsDao(): VaultSettingsDao
}
