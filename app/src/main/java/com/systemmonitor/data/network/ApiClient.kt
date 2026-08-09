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
import java.util.concurrent.TimeUnit

class ApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getStatus(baseUrl: String): NetworkResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/status")
                .get()
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("HTTP Error: ${response.code}")
            }
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
                val token = obj.optString("access_token")
                val msg = obj.optString("message", "Paired")
                NetworkResult.Success(PairingResponse(success = true, token = token, message = msg))
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
            if (!token.isNullOrEmpty()) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }
            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful && body.isNotEmpty()) {
                val root = JSONObject(body)
                val usage = parseTelemetryJson(root)
                NetworkResult.Success(usage)
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

            if (!token.isNullOrEmpty()) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }

            val response = client.newCall(reqBuilder.build()).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val obj = JSONObject(body)
                val msg = obj.optString("message", "Command executed successfully")
                NetworkResult.Success(msg)
            } else {
                val errObj = if (body.isNotEmpty()) JSONObject(body) else null
                val err = errObj?.optString("detail") ?: "Power command failed"
                NetworkResult.Error(err)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Command execution error", e)
        }
    }

    private fun parseTelemetryJson(root: JSONObject): UsageInfo {
        val cpuObj = root.optJSONObject("cpu") ?: JSONObject()
        val cpu = CpuInfo(
            processorName = cpuObj.optString("processor_name", "Intel Core i7"),
            usagePercent = cpuObj.optDouble("usage_percent", 0.0),
            logicalCores = cpuObj.optInt("logical_cores", 8),
            physicalCores = cpuObj.optInt("physical_cores", 4),
            frequencyMhz = cpuObj.optDouble("frequency_mhz", 2400.0)
        )

        val memObj = root.optJSONObject("memory") ?: JSONObject()
        val mem = MemoryInfo(
            totalBytes = memObj.optLong("total_bytes", 0),
            availableBytes = memObj.optLong("available_bytes", 0),
            usedBytes = memObj.optLong("used_bytes", 0),
            usagePercent = memObj.optDouble("usage_percent", 0.0)
        )

        val batObj = root.optJSONObject("battery") ?: JSONObject()
        val bat = LaptopBatteryInfo(
            hasBattery = batObj.optBoolean("has_battery", true),
            percent = batObj.optDouble("percent", 100.0),
            powerPlugged = batObj.optBoolean("power_plugged", true),
            status = batObj.optString("status", "AC Power")
        )

        val netObj = root.optJSONObject("network") ?: JSONObject()
        val net = LaptopNetworkInfo(
            hostname = netObj.optString("hostname", "Laptop"),
            primaryIp = netObj.optString("primary_ip", "192.168.1.50"),
            bytesSent = netObj.optLong("bytes_sent", 0),
            bytesRecv = netObj.optLong("bytes_recv", 0)
        )

        return UsageInfo(
            cpu = cpu,
            memory = mem,
            battery = bat,
            network = net,
            uptimeSeconds = root.optDouble("uptime_seconds", 0.0)
        )
    }
}

data class PairingResponse(
    val success: Boolean,
    val token: String?,
    val message: String
)
