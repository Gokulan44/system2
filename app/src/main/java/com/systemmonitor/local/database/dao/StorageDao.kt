package com.systemmonitor.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.local.database.entity.StorageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StorageEntity): Long

    @Query("SELECT * FROM storage_readings ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<StorageEntity?>

    @Query("SELECT * FROM storage_readings WHERE timestamp >= :sinceEpochMillis ORDER BY timestamp ASC")
    suspend fun getSince(sinceEpochMillis: Long): List<StorageEntity>

    @Query("DELETE FROM storage_readings WHERE timestamp < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)
}
