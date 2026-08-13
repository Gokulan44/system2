package com.systemmonitor.features.security.data.dao

import androidx.room.*
import com.systemmonitor.features.security.data.entity.SecurityScanEntity
import com.systemmonitor.features.security.data.entity.ThreatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityScanDao {
    @Query("SELECT * FROM security_scan_table ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<SecurityScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: SecurityScanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreats(threats: List<ThreatEntity>)

    @Query("SELECT * FROM threat_table WHERE scanId = :scanId")
    suspend fun getThreatsForScan(scanId: Long): List<ThreatEntity>

    @Query("SELECT * FROM security_scan_table WHERE scanId = :scanId")
    suspend fun getScanById(scanId: Long): SecurityScanEntity?

    @Query("DELETE FROM threat_table WHERE id = :threatId")
    suspend fun deleteThreat(threatId: String)
}
