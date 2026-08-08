package com.systemmonitor.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.local.database.entity.NetworkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NetworkEntity): Long

    @Query("SELECT * FROM network_readings ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<NetworkEntity?>

    @Query("SELECT * FROM network_readings WHERE timestamp >= :sinceEpochMillis ORDER BY timestamp ASC")
    suspend fun getSince(sinceEpochMillis: Long): List<NetworkEntity>

    @Query("DELETE FROM network_readings WHERE timestamp < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)
}
