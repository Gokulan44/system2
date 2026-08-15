package com.systemmonitor.connection

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class LocalConnection {
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun connect(targetIp: String, port: Int): Boolean {
        return try {
            val request = Request.Builder()
                .url("http://$targetIp:$port/api/status")
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    fun disconnect() {
        // Cleanup local socket connections
    }
}
