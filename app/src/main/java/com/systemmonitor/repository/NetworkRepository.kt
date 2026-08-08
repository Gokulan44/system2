package com.systemmonitor.repository

import com.systemmonitor.domain.mapper.toDomain
import com.systemmonitor.domain.mapper.toEntity
import com.systemmonitor.domain.model.NetworkInfo
import com.systemmonitor.domain.model.WifiInfo
import com.systemmonitor.local.database.dao.NetworkDao
import com.systemmonitor.local.database.dao.WifiDao
import com.systemmonitor.monitoring.NetworkMonitor
import com.systemmonitor.monitoring.WifiMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepository @Inject constructor(
    private val networkDao: NetworkDao,
    private val wifiDao: WifiDao,
    private val networkMonitor: NetworkMonitor,
    private val wifiMonitor: WifiMonitor
) {
    fun observeLatestNetwork(): Flow<NetworkInfo?> = networkDao.observeLatest().map { it?.toDomain() }
    fun observeLatestWifi(): Flow<WifiInfo?> = wifiDao.observeLatest().map { it?.toDomain() }

    suspend fun captureAndStore(): NetworkInfo {
        val network = networkMonitor.readCurrent()
        networkDao.insert(network.toEntity())

        wifiMonitor.readCurrent()?.let { wifi ->
            wifiDao.insert(wifi.toEntity())
        }
        return network
    }

    suspend fun getHistorySince(sinceEpochMillis: Long): List<NetworkInfo> =
        networkDao.getSince(sinceEpochMillis).map { it.toDomain() }

    suspend fun pruneOlderThan(beforeEpochMillis: Long) {
        networkDao.deleteOlderThan(beforeEpochMillis)
        wifiDao.deleteOlderThan(beforeEpochMillis)
    }
}
