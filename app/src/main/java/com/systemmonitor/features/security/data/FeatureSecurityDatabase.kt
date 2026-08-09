package com.systemmonitor.features.security.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.systemmonitor.features.security.data.dao.SecurityScanDao
import com.systemmonitor.features.security.data.entity.SecurityScanEntity
import com.systemmonitor.features.security.data.entity.ThreatEntity

@Database(
    entities = [
        SecurityScanEntity::class,
        ThreatEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FeatureSecurityDatabase : RoomDatabase() {
    abstract fun securityScanDao(): SecurityScanDao
}
