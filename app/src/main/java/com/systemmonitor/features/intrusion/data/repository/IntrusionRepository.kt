package com.systemmonitor.features.intrusion.data.repository

import com.systemmonitor.features.intrusion.data.dao.IntrusionEventDao
import com.systemmonitor.features.intrusion.data.entity.IntrusionEventEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntrusionRepository @Inject constructor(
    private val intrusionEventDao: IntrusionEventDao
) {
    val allEvents: Flow<List<IntrusionEventEntity>> = intrusionEventDao.getAllEvents()
    val unreadCount: Flow<Int> = intrusionEventDao.getUnreadCount()

    suspend fun insertEvent(event: IntrusionEventEntity) {
        intrusionEventDao.insertEvent(event)
    }

    suspend fun getEventById(eventId: String): IntrusionEventEntity? {
        return intrusionEventDao.getEventById(eventId)
    }

    suspend fun markAsRead(eventId: String) {
        intrusionEventDao.markEventAsRead(eventId)
    }

    suspend fun deleteEvent(eventId: String) {
        intrusionEventDao.deleteEventById(eventId)
    }

    suspend fun clearAllEvents() {
        intrusionEventDao.deleteAllEvents()
    }
}
