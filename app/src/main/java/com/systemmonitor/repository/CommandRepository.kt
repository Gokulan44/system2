package com.systemmonitor.repository

import com.systemmonitor.data.network.ConnectionManager
import com.systemmonitor.data.network.NetworkResult
import com.systemmonitor.domain.model.CommandType
import com.systemmonitor.domain.model.Laptop
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandRepository @Inject constructor(
    private val connectionManager: ConnectionManager
) {
    suspend fun sendPowerCommand(
        laptop: Laptop,
        commandType: CommandType,
        pin: String?
    ): NetworkResult<String> {
        return connectionManager.executePowerCommand(laptop, commandType, pin)
    }
}
