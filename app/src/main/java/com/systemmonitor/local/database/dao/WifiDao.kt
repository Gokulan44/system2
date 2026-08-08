package com.systemmonitor.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.local.database.entity.WifiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WifiEntity): Long

    @Query("SELECT * FROM wifi_readings ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<WifiEntity?>

    @Query("DELETE FROM wifi_readings WHERE timestamp < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)
}
