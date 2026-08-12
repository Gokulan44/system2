package com.systemmonitor.features.remotepermission.data.dao

import androidx.room.*
import com.systemmonitor.features.remotepermission.data.entity.PermissionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionHistoryDao {
    @Query("SELECT * FROM permission_history ORDER BY timestamp DESC")
    fun getHistoryFlow(): Flow<List<PermissionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: PermissionHistoryEntity)

    @Query("DELETE FROM permission_history")
    suspend fun clearHistory()
}
