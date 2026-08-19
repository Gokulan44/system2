package com.systemmonitor.securityscan.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.securityscan.database.entity.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM security_scan_history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM security_scan_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<ScanHistoryEntity>

    @Query("SELECT * FROM security_scan_history WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: Long): ScanHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistoryEntity): Long

    @Delete
    suspend fun deleteScan(scan: ScanHistoryEntity)

    @Query("DELETE FROM security_scan_history")
    suspend fun clearHistory()
}