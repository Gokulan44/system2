package com.systemmonitor.securityscan.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.systemmonitor.securityscan.database.dao.FindingDao
import com.systemmonitor.securityscan.database.dao.KnownHashDao
import com.systemmonitor.securityscan.database.dao.ScanHistoryDao
import com.systemmonitor.securityscan.database.entity.FindingEntity
import com.systemmonitor.securityscan.database.entity.KnownHashEntity
import com.systemmonitor.securityscan.database.entity.ScanHistoryEntity

@Database(
    entities = [
        ScanHistoryEntity::class,
        FindingEntity::class,
        KnownHashEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SecurityScanDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun findingDao(): FindingDao
    abstract fun knownHashDao(): KnownHashDao
}