package com.systemmonitor.features.intrusion.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.systemmonitor.features.intrusion.data.entity.IntrusionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntrusionEventDao {

    @Query("SELECT * FROM intrusion_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<IntrusionEventEntity>>

    @Query("SELECT * FROM intrusion_events WHERE eventId = :eventId LIMIT 1")
    suspend fun getEventById(eventId: String): IntrusionEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: IntrusionEventEntity)

    @Query("UPDATE intrusion_events SET isRead = 1 WHERE eventId = :eventId")
    suspend fun markEventAsRead(eventId: String)

    @Query("SELECT COUNT(*) FROM intrusion_events WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("DELETE FROM intrusion_events WHERE eventId = :eventId")
    suspend fun deleteEventById(eventId: String)

    @Query("DELETE FROM intrusion_events")
    suspend fun deleteAllEvents()
}
