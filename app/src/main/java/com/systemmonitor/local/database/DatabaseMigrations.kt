package com.systemmonitor.local.database

import androidx.room.migration.Migration

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
    val ALL = arrayOf<Migration>()
}
