package com.systemmonitor.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Central registry of all Room migrations. Keep every migration here (never
 * delete an old one) so upgrades from any historical version still resolve.
 *
 * Example for the future:
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE battery_readings ADD COLUMN cycleCount INTEGER NOT NULL DEFAULT 0")
 *     }
 * }
 */
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

    val ALL = arrayOf<Migration>(MIGRATION_3_4, MIGRATION_5_6)
}
