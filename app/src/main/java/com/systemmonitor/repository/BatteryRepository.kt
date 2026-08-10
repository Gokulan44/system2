package com.systemmonitor.repository

import com.systemmonitor.domain.mapper.toDomain
import com.systemmonitor.domain.mapper.toEntity
import com.systemmonitor.domain.model.Battery
import com.systemmonitor.local.database.dao.BatteryDao
import com.systemmonitor.monitoring.BatteryMonitor
import com.systemmonitor.features.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryRepository @Inject constructor(
    private val batteryDao: BatteryDao,
    private val batteryMonitor: BatteryMonitor,
    private val settingsRepository: SettingsRepository
) {

    /** Live UI stream — last persisted reading. */
    fun observeLatest(): Flow<Battery?> =
        batteryDao.observeLatest().map { it?.toDomain() }

    /** Reads fresh state from the OS and persists it. Called by the worker and pull-to-refresh. */
    suspend fun captureAndStore(): Battery? {
        val settings = settingsRepository.settingsFlow.value.power
        if (!settings.batteryMonitoringEnabled) {
            return null
        }
        val reading = batteryMonitor.readCurrent() ?: return null
        batteryDao.insert(reading.toEntity())
        return reading
    }

    suspend fun getHistorySince(sinceEpochMillis: Long): List<Battery> =
        batteryDao.getSince(sinceEpochMillis).map { it.toDomain() }

    /** Used by FirebaseSyncWorker to push a rollup instead of raw rows. */
    suspend fun getSummarySince(sinceEpochMillis: Long): BatterySummary {
        val avgLevel = batteryDao.getAverageLevelSince(sinceEpochMillis) ?: 0.0
        val avgTemp = batteryDao.getAverageTemperatureSince(sinceEpochMillis) ?: 0.0
        return BatterySummary(
            periodStart = sinceEpochMillis,
            averageLevelPercent = avgLevel,
            averageTemperatureCelsius = avgTemp
        )
    }

    suspend fun pruneOlderThan(beforeEpochMillis: Long) =
        batteryDao.deleteOlderThan(beforeEpochMillis)
}

data class BatterySummary(
    val periodStart: Long,
    val averageLevelPercent: Double,
    val averageTemperatureCelsius: Double
)
