package com.systemmonitor.data.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.systemmonitor.domain.model.ConnectionMode
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.ProcessInfo
import com.systemmonitor.domain.model.UsageInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore relay for long-distance (remote) connections.
 *
 * How it works:
 * - Android writes a command request to:
 *       /relay/{deviceId}/pending_commands/{commandId}
 * - Windows Agent reads these commands and executes them, then writes back to:
 *       /relay/{deviceId}/telemetry   (for status updates)
 *       /relay/{deviceId}/result/{commandId}  (for command responses)
 * - Android polls these paths to read the result.
 *
 * Firestore REST is used indirectly via the Firebase SDK (Kotlin/Android) —
 * no Admin SDK or service account JSON needed on the Android side.
 */
@Singleton
class RemoteRelayManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val RELAY_COLLECTION = "relay"
        private const val TELEMETRY_DOC = "telemetry"
        private const val COMMANDS_COLLECTION = "pending_commands"
        private const val RESULTS_COLLECTION = "results"
        private const val POLL_TIMEOUT_MS = 15_000L   // 15 seconds max wait
        private const val POLL_INTERVAL_MS = 1_500L    // check result every 1.5 s
    }

    /**
     * Push a telemetry request and wait for the Windows Agent to push the result back.
     */
    suspend fun fetchRemoteTelemetry(laptop: Laptop): NetworkResult<UsageInfo> {
        return try {
            val cmdId = "tel_${System.currentTimeMillis()}"
            val deviceRef = firestore.collection(RELAY_COLLECTION).document(laptop.id)

            // 1. Write the command
            deviceRef.collection(COMMANDS_COLLECTION).document(cmdId).set(
                mapOf(
                    "type" to "GET_TELEMETRY",
                    "requestedAt" to System.currentTimeMillis(),
                    "androidDeviceId" to (auth.currentUser?.uid ?: "unknown")
                )
            ).await()

            // 2. Poll for result (Windows Agent writes it to telemetry doc)
            val result = withTimeoutOrNull(POLL_TIMEOUT_MS) {
                var telemetry: UsageInfo? = null
                while (telemetry == null) {
                    val snap = deviceRef.get().await()
                    val lastUpdated = snap.getLong("lastUpdated") ?: 0L
                    val age = System.currentTimeMillis() - lastUpdated
                    if (age < 10_000L) { // Accept if updated within 10s
                        val raw = snap.getString("telemetryJson")
                        if (!raw.isNullOrEmpty()) {
                            telemetry = parseTelemetryJson(raw)
                        }
                    }
                    if (telemetry == null) delay(POLL_INTERVAL_MS)
                }
                telemetry
            }

            if (result != null) {
                NetworkResult.Success(result)
            } else {
                NetworkResult.Error("Remote agent did not respond in time. Ensure the Windows Agent is running and connected to the internet.")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Remote telemetry error")
        }
    }

    /**
     * Push a power command (lock / sleep / restart / shutdown) via Firestore.
     */
    suspend fun executePowerCommand(
        laptop: Laptop,
        commandType: String,
        pin: String?
    ): NetworkResult<String> {
        return try {
            val cmdId = "cmd_${System.currentTimeMillis()}"
            val deviceRef = firestore.collection(RELAY_COLLECTION).document(laptop.id)

            deviceRef.collection(COMMANDS_COLLECTION).document(cmdId).set(
                mapOf(
                    "type" to "POWER",
                    "action" to commandType,
                    "pin" to (pin ?: ""),
                    "requestedAt" to System.currentTimeMillis(),
                    "androidDeviceId" to (auth.currentUser?.uid ?: "unknown")
                )
            ).await()

            val result = withTimeoutOrNull(POLL_TIMEOUT_MS) {
                var response: String? = null
                while (response == null) {
                    val snap = deviceRef.collection(RESULTS_COLLECTION).document(cmdId).get().await()
                    if (snap.exists()) {
                        response = snap.getString("message") ?: "Command executed"
                    }
                    if (response == null) delay(POLL_INTERVAL_MS)
                }
                response
            }

            if (result != null) {
                NetworkResult.Success(result)
            } else {
                NetworkResult.Error("Remote agent did not confirm the command.")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Remote command error")
        }
    }

    /**
     * Fetch process list via Firestore relay.
     */
    suspend fun fetchRemoteProcesses(laptop: Laptop): NetworkResult<List<ProcessInfo>> {
        return try {
            val cmdId = "proc_${System.currentTimeMillis()}"
            val deviceRef = firestore.collection(RELAY_COLLECTION).document(laptop.id)

            deviceRef.collection(COMMANDS_COLLECTION).document(cmdId).set(
                mapOf(
                    "type" to "GET_PROCESSES",
                    "requestedAt" to System.currentTimeMillis()
                )
            ).await()

            val result = withTimeoutOrNull(POLL_TIMEOUT_MS) {
                var procs: List<ProcessInfo>? = null
                while (procs == null) {
                    val snap = deviceRef.collection(RESULTS_COLLECTION).document(cmdId).get().await()
                    if (snap.exists()) {
                        val raw = snap.getString("processesJson")
                        if (!raw.isNullOrEmpty()) {
                            procs = parseProcesses(raw)
                        }
                    }
                    if (procs == null) delay(POLL_INTERVAL_MS)
                }
                procs
            }

            if (result != null) {
                NetworkResult.Success(result)
            } else {
                NetworkResult.Error("Remote process list unavailable.")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Remote processes error")
        }
    }

    /**
     * Check if the remote relay is active (Windows Agent has registered presence recently).
     */
    suspend fun checkRemoteStatus(deviceId: String): NetworkResult<Boolean> {
        return try {
            val snap = firestore.collection(RELAY_COLLECTION).document(deviceId).get().await()
            val lastSeen = snap.getLong("agentLastSeen") ?: 0L
            val isAlive = (System.currentTimeMillis() - lastSeen) < 60_000L // alive if seen in last 60 s
            NetworkResult.Success(isAlive)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Remote status check failed")
        }
    }

    /**
     * Look up the pairing code in Firestore to retrieve the remote device details.
     */
    suspend fun verifyRemotePairing(pairingCode: String): NetworkResult<PairingResponse> {
        return try {
            val snap = firestore.collection("pairing").document(pairingCode).get().await()
            if (snap.exists()) {
                val deviceId = snap.getString("deviceId")
                val deviceName = snap.getString("deviceName") ?: "Remote Laptop"
                val createdAt = snap.getLong("createdAt") ?: 0L
                val age = System.currentTimeMillis() - createdAt
                if (deviceId.isNullOrEmpty()) {
                    NetworkResult.Error("Pairing document exists but deviceId is missing.")
                } else if (age > 300_000L) {
                    NetworkResult.Error("Pairing code has expired.")
                } else {
                    try {
                        firestore.collection("pairing").document(pairingCode).delete().await()
                    } catch (e: Exception) {
                        // Ignore deletion error
                    }
                    NetworkResult.Success(
                        PairingResponse(
                            success = true,
                            token = "remote_token_$deviceId",
                            message = "Paired with $deviceName remotely",
                            deviceId = deviceId
                        )
                    )
                }
            } else {
                NetworkResult.Error("Invalid pairing code. Make sure the Windows Agent is running and connected to the internet.")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Remote pairing error", e)
        }
    }

    // --- Parsers ---

    private fun parseTelemetryJson(json: String): UsageInfo? {
        return try {
            val root = JSONObject(json)
            ApiClientParser.parseTelemetryJson(root)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseProcesses(json: String): List<ProcessInfo> {
        return try {
            ApiClientParser.parseProcessesJson(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
