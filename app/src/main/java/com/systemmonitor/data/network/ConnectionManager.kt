package com.systemmonitor.data.network

import com.systemmonitor.domain.model.CommandType
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.UsageInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionManager @Inject constructor() {
    private val apiClient = ApiClient()

    fun getBaseUrl(laptop: Laptop): String {
        return "http://${laptop.ipAddress}:${laptop.port}"
    }

    fun getWebSocketStreamUrl(laptop: Laptop): String {
        return "ws://${laptop.ipAddress}:${laptop.port}/ws/stream"
    }

    suspend fun checkStatus(laptop: Laptop): NetworkResult<Boolean> {
        val url = getBaseUrl(laptop)
        return apiClient.getStatus(url)
    }

    suspend fun verifyPairing(
        ipAddress: String,
        port: Int,
        pairingCode: String,
        deviceName: String,
        deviceId: String
    ): NetworkResult<PairingResponse> {
        val url = "http://$ipAddress:$port"
        return apiClient.verifyPairing(url, pairingCode, deviceName, deviceId)
    }

    suspend fun fetchTelemetry(laptop: Laptop): NetworkResult<UsageInfo> {
        val url = getBaseUrl(laptop)
        return apiClient.fetchTelemetry(url, laptop.accessToken)
    }

    suspend fun executePowerCommand(
        laptop: Laptop,
        command: CommandType,
        pin: String?
    ): NetworkResult<String> {
        val url = getBaseUrl(laptop)
        return apiClient.executePowerCommand(url, laptop.accessToken, command, pin)
    }
}
