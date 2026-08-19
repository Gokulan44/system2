package com.systemmonitor.securityscan.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.securityscan.database.entity.FindingEntity

@Dao
interface FindingDao {
    @Query("SELECT * FROM security_scan_findings WHERE scanId = :scanId")
    suspend fun getFindingsForScan(scanId: Long): List<FindingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFindings(findings: List<FindingEntity>)

    @Query("DELETE FROM security_scan_findings WHERE scanId = :scanId")
    suspend fun deleteFindingsForScan(scanId: Long)
}