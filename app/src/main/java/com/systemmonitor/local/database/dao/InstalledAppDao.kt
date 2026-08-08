package com.systemmonitor.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.local.database.entity.InstalledAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledAppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<InstalledAppEntity>)

    @Query("SELECT * FROM installed_apps ORDER BY dangerousPermissions DESC")
    fun observeAll(): Flow<List<InstalledAppEntity>>

    @Query("SELECT * FROM installed_apps WHERE isSystemApp = 0 ORDER BY dangerousPermissions DESC")
    suspend fun getNonSystemApps(): List<InstalledAppEntity>

    @Query("SELECT COUNT(*) FROM installed_apps WHERE installerPackageName IS NULL AND isSystemApp = 0")
    suspend fun countSideloadedApps(): Int

    @Query("DELETE FROM installed_apps WHERE lastScannedTimestamp < :beforeEpochMillis")
    suspend fun deleteStale(beforeEpochMillis: Long)
}
