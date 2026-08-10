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

    val ALL = arrayOf<Migration>(MIGRATION_3_4)
}
