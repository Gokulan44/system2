package com.systemmonitor.securityanalysis.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity): Long

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreats(threats: List<ThreatEntity>)

    @Query("SELECT * FROM detected_threats WHERE scanId = :scanId")
    suspend fun getThreatsForScan(scanId: Long): List<ThreatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuarantine(quarantine: QuarantineEntity): Long

    @Query("SELECT * FROM quarantine_vault WHERE isRestored = 0 ORDER BY timestamp DESC")
    fun getActiveQuarantineFiles(): Flow<List<QuarantineEntity>>

    @Query("UPDATE quarantine_vault SET isRestored = 1 WHERE id = :id")
    suspend fun markRestored(id: Long)

    @Query("DELETE FROM quarantine_vault WHERE id = :id")
    suspend fun deleteQuarantineRecord(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHashes(hashes: List<HashEntity>)

    @Query("SELECT * FROM threat_signatures WHERE sha256 = :sha256 LIMIT 1")
    suspend fun lookupHash(sha256: String): HashEntity?
}
