package com.systemmonitor.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    /** v3 → v4: adds connectionMode column to laptops table (LOCAL | REMOTE) */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE laptops ADD COLUMN connectionMode TEXT NOT NULL DEFAULT 'LOCAL'"
            )
        }
    }

    /** v5 → v6: adds unlock_history table */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `unlock_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `laptopId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `result` TEXT NOT NULL, `method` TEXT NOT NULL, `reason` TEXT)"
            )
        }
    }

    /** v10 → v11: adds fileHash, checksum, isTrash, trashedAt to vault_files, isTrash to vault_folders, and vault_settings table */
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE vault_files ADD COLUMN fileHash TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE vault_files ADD COLUMN checksum TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE vault_files ADD COLUMN isTrash INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE vault_files ADD COLUMN trashedAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE vault_folders ADD COLUMN isTrash INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE TABLE IF NOT EXISTS `vault_settings` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))")
        }
    }

    val ALL = arrayOf<Migration>(MIGRATION_3_4, MIGRATION_5_6, MIGRATION_10_11)
}
