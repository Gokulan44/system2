package com.systemmonitor.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.local.database.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MemoryEntity): Long

    @Query("SELECT * FROM memory_readings ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<MemoryEntity?>

    @Query("SELECT * FROM memory_readings WHERE timestamp >= :sinceEpochMillis ORDER BY timestamp ASC")
    suspend fun getSince(sinceEpochMillis: Long): List<MemoryEntity>

    @Query("SELECT AVG(usedMb) FROM memory_readings WHERE timestamp >= :sinceEpochMillis")
    suspend fun getAverageUsedSince(sinceEpochMillis: Long): Double?

    @Query("DELETE FROM memory_readings WHERE timestamp < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)
}
