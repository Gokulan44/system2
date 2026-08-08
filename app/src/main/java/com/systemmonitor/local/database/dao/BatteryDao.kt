package com.systemmonitor.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.local.database.entity.BatteryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BatteryEntity): Long

    @Query("SELECT * FROM battery_readings ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<BatteryEntity?>

    @Query("SELECT * FROM battery_readings WHERE timestamp >= :sinceEpochMillis ORDER BY timestamp ASC")
    suspend fun getSince(sinceEpochMillis: Long): List<BatteryEntity>

    @Query("SELECT AVG(levelPercent) FROM battery_readings WHERE timestamp >= :sinceEpochMillis")
    suspend fun getAverageLevelSince(sinceEpochMillis: Long): Double?

    @Query("SELECT AVG(temperatureCelsius) FROM battery_readings WHERE timestamp >= :sinceEpochMillis")
    suspend fun getAverageTemperatureSince(sinceEpochMillis: Long): Double?

    @Query("DELETE FROM battery_readings WHERE timestamp < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)
}
