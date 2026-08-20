package com.systemmonitor.vault.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        VaultFolderEntity::class,
        VaultFileEntity::class,
        VaultAuditEntity::class,
        VaultSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultFolderDao(): VaultFolderDao
    abstract fun vaultFileDao(): VaultFileDao
    abstract fun vaultAuditDao(): VaultAuditDao
    abstract fun vaultSettingsDao(): VaultSettingsDao
}
