package com.systemmonitor.repository

import com.systemmonitor.data.network.ConnectionManager
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.data.network.PairingResponse
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.LaptopStatus
import com.systemmonitor.domain.model.UsageInfo
import com.systemmonitor.local.database.dao.LaptopDao
import com.systemmonitor.local.database.entity.LaptopEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LaptopRepository @Inject constructor(
    private val laptopDao: LaptopDao,
    private val connectionManager: ConnectionManager
) {
    val allLaptops: Flow<List<Laptop>> = laptopDao.getAllLaptops().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun pairLaptop(
        ipAddress: String,
        port: Int,
        pairingCode: String,
        deviceName: String,
        deviceId: String
    ): NetworkResult<Laptop> {
        val res = connectionManager.verifyPairing(ipAddress, port, pairingCode, deviceName, deviceId)
        return when (res) {
            is NetworkResult.Success -> {
                val newLaptop = Laptop(
                    id = deviceId,
                    name = deviceName,
                    ipAddress = ipAddress,
                    port = port,
                    status = LaptopStatus.ONLINE,
                    isLocalConnection = true,
                    accessToken = res.data.token,
                    lastSeen = System.currentTimeMillis()
                )
                laptopDao.insertLaptop(newLaptop.toEntity())
                NetworkResult.Success(newLaptop)
            }
            is NetworkResult.Error -> NetworkResult.Error(res.message, res.cause)
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun fetchTelemetry(laptop: Laptop): NetworkResult<UsageInfo> {
        return connectionManager.fetchTelemetry(laptop)
    }

    suspend fun deleteLaptop(laptopId: String) {
        laptopDao.deleteLaptopById(laptopId)
    }

    private fun LaptopEntity.toDomain() = Laptop(
        id = id,
        name = name,
        ipAddress = ipAddress,
        port = port,
        os = os,
        status = runCatching { LaptopStatus.valueOf(status) }.getOrDefault(LaptopStatus.OFFLINE),
        isLocalConnection = isLocalConnection,
        accessToken = accessToken,
        lastSeen = lastSeen
    )

    private fun Laptop.toEntity() = LaptopEntity(
        id = id,
        name = name,
        ipAddress = ipAddress,
        port = port,
        os = os,
        status = status.name,
        isLocalConnection = isLocalConnection,
        accessToken = accessToken,
        lastSeen = lastSeen
    )
}
