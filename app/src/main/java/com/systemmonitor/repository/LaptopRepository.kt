package com.systemmonitor.repository

import com.systemmonitor.data.network.ConnectionManager
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.data.network.PairingResponse
import com.systemmonitor.domain.model.ConnectionMode
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.LaptopStatus
import com.systemmonitor.domain.model.UsageInfo
import com.systemmonitor.local.database.dao.LaptopDao
import com.systemmonitor.local.database.entity.LaptopEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LaptopRepository @Inject constructor(
    private val laptopDao: LaptopDao,
    private val connectionManager: ConnectionManager,
    private val unlockHistoryDao: com.systemmonitor.local.database.dao.UnlockHistoryDao,
    private val permissionRequestManager: com.systemmonitor.features.remotepermission.request.PermissionRequestManager
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        startAutomaticConnectionWatcher()
    }

    private fun startAutomaticConnectionWatcher() {
        repositoryScope.launch {
            while (true) {
                try {
                    val laptopsList = laptopDao.getAllLaptopsList().map { it.toDomain() }
                    permissionRequestManager.startWatching(laptopsList)

                    for (laptop in laptopsList) {
                        val checkResult = connectionManager.checkStatus(laptop)
                        val newStatus = if (checkResult is NetworkResult.Success && checkResult.data) {
                            LaptopStatus.ONLINE
                        } else {
                            LaptopStatus.OFFLINE
                        }
                        
                        var lockChanged = false
                        var currentLockState = laptop.isLocked
                        
                        if (newStatus == LaptopStatus.ONLINE) {
                            val lockRes = connectionManager.getLockStatus(laptop)
                            if (lockRes is NetworkResult.Success) {
                                val agentLocked = lockRes.data
                                if (laptop.isLocked != agentLocked) {
                                    currentLockState = agentLocked
                                    lockChanged = true
                                    laptopDao.updateLaptopLockStatus(laptop.id, agentLocked)
                                    // Log status change
                                    val method = "PHYSICAL"
                                    val resultText = if (agentLocked) "LOCKED" else "SUCCESS"
                                    val reasonText = if (agentLocked) "Workstation locked physically" else "Workstation unlocked physically"
                                    logUnlockAttempt(laptop.id, method, resultText, reasonText)
                                }
                            }
                        }

                        if (laptop.status != newStatus || lockChanged) {
                            laptopDao.updateLaptopStatus(laptop.id, newStatus.name, System.currentTimeMillis())
                        }
                    }
                } catch (e: Exception) {
                    // Prevent watcher thread crash on exceptions
                }
                delay(10_000L)
            }
        }
    }

    val allLaptops: Flow<List<Laptop>> = laptopDao.getAllLaptops().map { entities ->

        entities.map { it.toDomain() }
    }

    suspend fun pairLaptop(
        ipAddress: String,
        port: Int,
        pairingCode: String,
        deviceName: String,
        deviceId: String,
        connectionMode: ConnectionMode = ConnectionMode.LOCAL
    ): NetworkResult<Laptop> {
        val res = connectionManager.verifyPairing(ipAddress, port, pairingCode, deviceName, deviceId, connectionMode)
        return when (res) {
            is NetworkResult.Success -> {
                val finalDeviceId = res.data.deviceId ?: deviceId
                val newLaptop = Laptop(
                    id = finalDeviceId,
                    name = deviceName,
                    ipAddress = ipAddress,
                    port = port,
                    status = LaptopStatus.ONLINE,
                    isLocalConnection = connectionMode == ConnectionMode.LOCAL,
                    connectionMode = connectionMode,
                    accessToken = res.data.token,
                    lastSeen = System.currentTimeMillis(),
                    macAddress = res.data.macAddress
                )
                laptopDao.insertLaptop(newLaptop.toEntity())
                NetworkResult.Success(newLaptop)
            }
            is NetworkResult.Error -> NetworkResult.Error(res.message, res.cause)
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun updateConnectionMode(laptopId: String, mode: ConnectionMode) {
        laptopDao.updateConnectionMode(laptopId, mode.name)
    }

    suspend fun checkStatus(ipAddress: String, port: Int): NetworkResult<Boolean> {
        return connectionManager.checkStatus(ipAddress, port)
    }

    suspend fun checkStatusForLaptop(laptop: Laptop): NetworkResult<Boolean> {
        return connectionManager.checkStatus(laptop)
    }

    suspend fun fetchTelemetry(laptop: Laptop): NetworkResult<UsageInfo> {
        return connectionManager.fetchTelemetry(laptop)
    }

    suspend fun fetchProcesses(laptop: Laptop): NetworkResult<List<com.systemmonitor.domain.model.ProcessInfo>> {
        return connectionManager.fetchProcesses(laptop)
    }

    suspend fun deleteLaptop(laptopId: String) {
        laptopDao.deleteLaptopById(laptopId)
    }

    suspend fun getAllLaptopsList(): List<Laptop> {
        return laptopDao.getAllLaptopsList().map { it.toDomain() }
    }

    suspend fun updateLaptopStatusAndMode(laptopId: String, status: LaptopStatus, mode: ConnectionMode) {
        val entity = laptopDao.getLaptopById(laptopId) ?: return
        laptopDao.insertLaptop(entity.copy(
            status = status.name,
            connectionMode = mode.name,
            lastSeen = System.currentTimeMillis()
        ))
    }

    suspend fun updateLaptopStatus(laptopId: String, status: LaptopStatus) {
        val entity = laptopDao.getLaptopById(laptopId) ?: return
        laptopDao.insertLaptop(entity.copy(
            status = status.name,
            lastSeen = System.currentTimeMillis()
        ))
    }

    private fun LaptopEntity.toDomain() = Laptop(
        id = id,
        name = name,
        ipAddress = ipAddress,
        port = port,
        os = os,
        status = runCatching { LaptopStatus.valueOf(status) }.getOrDefault(LaptopStatus.OFFLINE),
        isLocalConnection = isLocalConnection,
        connectionMode = runCatching { ConnectionMode.valueOf(connectionMode) }
            .getOrDefault(ConnectionMode.LOCAL),
        accessToken = accessToken,
        lastSeen = lastSeen,
        macAddress = macAddress,
        isLocked = isLocked
    )

    private fun Laptop.toEntity() = LaptopEntity(
        id = id,
        name = name,
        ipAddress = ipAddress,
        port = port,
        os = os,
        status = status.name,
        isLocalConnection = isLocalConnection,
        connectionMode = connectionMode.name,
        accessToken = accessToken,
        lastSeen = lastSeen,
        macAddress = macAddress,
        isLocked = isLocked
    )

    suspend fun getUnlockChallenge(laptop: Laptop): NetworkResult<String> {
        return connectionManager.fetchUnlockChallenge(laptop)
    }

    suspend fun submitUnlockSignature(
        laptop: Laptop,
        challenge: String,
        signature: String,
        publicKey: String
    ): NetworkResult<Boolean> {
        return connectionManager.submitUnlockSignature(laptop, challenge, signature, publicKey)
    }

    suspend fun logUnlockAttempt(laptopId: String, method: String, result: String, reason: String? = null) {
        val entity = com.systemmonitor.local.database.entity.UnlockHistoryEntity(
            laptopId = laptopId,
            timestamp = System.currentTimeMillis(),
            result = result,
            method = method,
            reason = reason
        )
        unlockHistoryDao.insert(entity)
    }

    fun getUnlockHistory(laptopId: String): Flow<List<com.systemmonitor.local.database.entity.UnlockHistoryEntity>> {
        return unlockHistoryDao.getHistoryForLaptop(laptopId)
    }

    suspend fun updateLaptopLockStatus(laptopId: String, isLocked: Boolean) {
        laptopDao.updateLaptopLockStatus(laptopId, isLocked)
    }
}
