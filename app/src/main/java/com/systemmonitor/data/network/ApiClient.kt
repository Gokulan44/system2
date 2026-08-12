package com.systemmonitor.data.network

import com.systemmonitor.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val client: OkHttpClient
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getStatus(baseUrl: String): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$baseUrl/api/status").get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("HTTP Error: ${response.code}")
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Connection failed", e)
        }
    }

    suspend fun verifyPairing(
        baseUrl: String,
        pairingCode: String,
        deviceName: String,
        deviceId: String
    ): NetworkResult<PairingResponse> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("pairing_code", pairingCode)
                put("device_name", deviceName)
                put("device_id", deviceId)
            }
            val request = Request.Builder()
                .url("$baseUrl/api/pairing/verify")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val obj = JSONObject(body)
                NetworkResult.Success(
                    PairingResponse(
                        success = true,
                        token = obj.optString("access_token"),
                        message = obj.optString("message", "Paired"),
                        deviceId = obj.optString("device_id"),
                        macAddress = obj.optString("mac_address").takeIf { it.isNotEmpty() }
                    )
                )
            } else {
                val err = if (body.isNotEmpty()) JSONObject(body).optString("detail", "Pairing failed") else "Pairing failed"
                NetworkResult.Error(err)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to reach agent", e)
        }
    }

    suspend fun fetchTelemetry(baseUrl: String, token: String?): NetworkResult<UsageInfo> = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder().url("$baseUrl/api/telemetry").get()
            if (!token.isNullOrEmpty()) reqBuilder.addHeader("Authorization", "Bearer $token")
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful && body.isNotEmpty()) {
                NetworkResult.Success(ApiClientParser.parseTelemetryJson(JSONObject(body)))
            } else {
                NetworkResult.Error("Failed to fetch telemetry")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Telemetry error", e)
        }
    }

    suspend fun executePowerCommand(
        baseUrl: String,
        token: String?,
        command: CommandType,
        pin: String?
    ): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("action", command.name.lowercase())
                if (!pin.isNullOrEmpty()) put("pin", pin)
            }
            val reqBuilder = Request.Builder()
                .url("$baseUrl/api/power")
                .post(json.toString().toRequestBody(jsonMediaType))
            if (!token.isNullOrEmpty()) reqBuilder.addHeader("Authorization", "Bearer $token")
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                NetworkResult.Success(JSONObject(body).optString("message", "Command executed successfully"))
            } else {
                val errObj = if (body.isNotEmpty()) JSONObject(body) else null
                NetworkResult.Error(errObj?.optString("detail") ?: "Power command failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Command execution error", e)
        }
    }

    suspend fun fetchProcesses(baseUrl: String, token: String?): NetworkResult<List<ProcessInfo>> = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder().url("$baseUrl/api/processes").get()
            if (!token.isNullOrEmpty()) reqBuilder.addHeader("Authorization", "Bearer $token")
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful && body.isNotEmpty()) {
                NetworkResult.Success(ApiClientParser.parseProcessesJson(body))
            } else {
                NetworkResult.Error("Failed to fetch processes")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Processes error", e)
        }
    }

    suspend fun getUnlockChallenge(baseUrl: String, token: String?): NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder().url("$baseUrl/api/unlock/challenge").get()
            if (!token.isNullOrEmpty()) reqBuilder.addHeader("Authorization", "Bearer $token")
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful && body.isNotEmpty()) {
                val obj = JSONObject(body)
                NetworkResult.Success(obj.optString("challenge", ""))
            } else {
                NetworkResult.Success("challenge_mock_${System.currentTimeMillis()}")
            }
        } catch (e: Exception) {
            NetworkResult.Success("challenge_mock_${System.currentTimeMillis()}")
        }
    }

    suspend fun submitUnlockSignature(
        baseUrl: String,
        token: String?,
        challenge: String,
        signature: String,
        publicKey: String
    ): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("challenge", challenge)
                put("signature", signature)
                put("public_key", publicKey)
            }
            val reqBuilder = Request.Builder()
                .url("$baseUrl/api/unlock/verify")
                .post(json.toString().toRequestBody(jsonMediaType))
            if (!token.isNullOrEmpty()) reqBuilder.addHeader("Authorization", "Bearer $token")
            val response = client.newCall(reqBuilder.build()).execute()
            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("Verification failed: ${response.code}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to verify signature", e)
        }
    }

    suspend fun approveResource(
        baseUrl: String,
        token: String?,
        approvalTokenJson: String
    ): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/resource/approve")
                .post(approvalTokenJson.toRequestBody(jsonMediaType))
            if (!token.isNullOrEmpty()) request.addHeader("Authorization", "Bearer $token")
            val response = client.newCall(request.build()).execute()
            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                val body = response.body?.string() ?: ""
                val err = if (body.isNotEmpty()) JSONObject(body).optString("detail", "Rejection") else "Rejection"
                NetworkResult.Error("Laptop rejected approval: $err")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Failed to send approval to laptop", e)
        }
    }
}

data class PairingResponse(
    val success: Boolean,
    val token: String?,
    val message: String,
    val deviceId: String? = null,
    val macAddress: String? = null
)
