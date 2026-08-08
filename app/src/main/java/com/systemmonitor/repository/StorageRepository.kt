package com.systemmonitor.repository

import com.systemmonitor.domain.mapper.toDomain
import com.systemmonitor.domain.mapper.toEntity
import com.systemmonitor.domain.model.Storage
import com.systemmonitor.local.database.dao.StorageDao
import com.systemmonitor.monitoring.StorageMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val storageDao: StorageDao,
    private val storageMonitor: StorageMonitor
) {
    fun observeLatest(): Flow<Storage?> = storageDao.observeLatest().map { it?.toDomain() }

    suspend fun captureAndStore(): Storage {
        val reading = storageMonitor.readCurrent()
        storageDao.insert(reading.toEntity())
        return reading
    }

    suspend fun getHistorySince(sinceEpochMillis: Long): List<Storage> =
        storageDao.getSince(sinceEpochMillis).map { it.toDomain() }

    suspend fun pruneOlderThan(beforeEpochMillis: Long) = storageDao.deleteOlderThan(beforeEpochMillis)
}
