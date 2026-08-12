package com.systemmonitor.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.systemmonitor.local.database.entity.UnlockHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockHistoryDao {
    @Insert
    suspend fun insert(entity: UnlockHistoryEntity)

    @Query("SELECT * FROM unlock_history WHERE laptopId = :laptopId ORDER BY timestamp DESC")
    fun getHistoryForLaptop(laptopId: String): Flow<List<UnlockHistoryEntity>>

    @Query("SELECT * FROM unlock_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<UnlockHistoryEntity>>
}
