package com.systemmonitor.repository

import com.systemmonitor.domain.mapper.toDomain
import com.systemmonitor.domain.mapper.toEntity
import com.systemmonitor.domain.model.Memory
import com.systemmonitor.local.database.dao.MemoryDao
import com.systemmonitor.monitoring.MemoryMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    private val memoryDao: MemoryDao,
    private val memoryMonitor: MemoryMonitor
) {
    fun observeLatest(): Flow<Memory?> = memoryDao.observeLatest().map { it?.toDomain() }

    suspend fun captureAndStore(): Memory {
        val reading = memoryMonitor.readCurrent()
        memoryDao.insert(reading.toEntity())
        return reading
    }

    suspend fun getHistorySince(sinceEpochMillis: Long): List<Memory> =
        memoryDao.getSince(sinceEpochMillis).map { it.toDomain() }

    suspend fun getAverageUsedMbSince(sinceEpochMillis: Long): Double =
        memoryDao.getAverageUsedSince(sinceEpochMillis) ?: 0.0

    suspend fun pruneOlderThan(beforeEpochMillis: Long) = memoryDao.deleteOlderThan(beforeEpochMillis)
}
