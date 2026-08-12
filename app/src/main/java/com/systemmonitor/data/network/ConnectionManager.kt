package com.systemmonitor.data.network

import com.systemmonitor.domain.model.CommandType
import com.systemmonitor.domain.model.ConnectionMode
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.ProcessInfo
import com.systemmonitor.domain.model.UsageInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Routes every operation to the correct transport layer:
 *  - LOCAL  → direct HTTP calls to the Windows Agent at 192.168.x.x:8765
 *  - REMOTE → Firebase Firestore relay (long distance, different networks)
 */
@Singleton
class ConnectionManager @Inject constructor(
    private val apiClient: ApiClient,
    private val remoteRelay: RemoteRelayManager
) {
    fun getBaseUrl(laptop: Laptop): String = "http://${laptop.ipAddress}:${laptop.port}"

    fun getWebSocketStreamUrl(laptop: Laptop): String =
        "ws://${laptop.ipAddress}:${laptop.port}/ws/stream"

    suspend fun checkStatus(laptop: Laptop): NetworkResult<Boolean> {
        return when (laptop.connectionMode) {
            ConnectionMode.LOCAL -> apiClient.getStatus(getBaseUrl(laptop))
            ConnectionMode.REMOTE -> remoteRelay.checkRemoteStatus(laptop.id)
        }
    }

    suspend fun checkStatus(ipAddress: String, port: Int): NetworkResult<Boolean> {
        return apiClient.getStatus("http://$ipAddress:$port")
    }

    suspend fun verifyPairing(
        ipAddress: String,
        port: Int,
        pairingCode: String,
        deviceName: String,
        deviceId: String,
        connectionMode: ConnectionMode
    ): NetworkResult<PairingResponse> {
        return when (connectionMode) {
            ConnectionMode.LOCAL -> apiClient.verifyPairing("http://$ipAddress:$port", pairingCode, deviceName, deviceId)
            ConnectionMode.REMOTE -> remoteRelay.verifyRemotePairing(pairingCode)
        }
    }

    suspend fun fetchTelemetry(laptop: Laptop): NetworkResult<UsageInfo> {
        return when (laptop.connectionMode) {
            ConnectionMode.LOCAL -> apiClient.fetchTelemetry(getBaseUrl(laptop), laptop.accessToken)
            ConnectionMode.REMOTE -> remoteRelay.fetchRemoteTelemetry(laptop)
        }
    }

    suspend fun fetchProcesses(laptop: Laptop): NetworkResult<List<ProcessInfo>> {
        return when (laptop.connectionMode) {
            ConnectionMode.LOCAL -> apiClient.fetchProcesses(getBaseUrl(laptop), laptop.accessToken)
            ConnectionMode.REMOTE -> remoteRelay.fetchRemoteProcesses(laptop)
        }
    }

    suspend fun executePowerCommand(
        laptop: Laptop,
        command: CommandType,
        pin: String?
    ): NetworkResult<String> {
        return when (laptop.connectionMode) {
            ConnectionMode.LOCAL -> apiClient.executePowerCommand(
                getBaseUrl(laptop), laptop.accessToken, command, pin
            )
            ConnectionMode.REMOTE -> remoteRelay.executePowerCommand(
                laptop, command.name.lowercase(), pin
            )
        }
    }

    suspend fun fetchUnlockChallenge(laptop: Laptop): NetworkResult<String> {
        return when (laptop.connectionMode) {
            ConnectionMode.LOCAL -> apiClient.getUnlockChallenge(getBaseUrl(laptop), laptop.accessToken)
            ConnectionMode.REMOTE -> NetworkResult.Success("challenge_remote_mock_${System.currentTimeMillis()}")
        }
    }

    suspend fun submitUnlockSignature(
        laptop: Laptop,
        challenge: String,
        signature: String,
        publicKey: String
    ): NetworkResult<Boolean> {
        return when (laptop.connectionMode) {
            ConnectionMode.LOCAL -> apiClient.submitUnlockSignature(
                getBaseUrl(laptop), laptop.accessToken, challenge, signature, publicKey
            )
            ConnectionMode.REMOTE -> NetworkResult.Success(true)
        }
    }

    suspend fun approveResource(
        laptop: Laptop,
        approvalTokenJson: String
    ): NetworkResult<Boolean> {
        return when (laptop.connectionMode) {
            ConnectionMode.LOCAL -> apiClient.approveResource(getBaseUrl(laptop), laptop.accessToken, approvalTokenJson)
            ConnectionMode.REMOTE -> remoteRelay.approveResourceRemote(laptop.id, approvalTokenJson)
        }
    }
}
