package com.systemmonitor.securityanalysis.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScanEntity::class,
        ThreatEntity::class,
        QuarantineEntity::class,
        HashEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SecurityDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
}
