package com.systemmonitor.connection

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

class ReconnectionManager {
    private var attempt = 0

    suspend fun <T> executeWithRetry(block: suspend () -> T): Result<T> {
        attempt = 0
        while (attempt < 5) {
            try {
                return Result.success(block())
            } catch (e: Exception) {
                attempt++
                val delayMs = min(30000L, (2.0.pow(attempt.toDouble()) * 1000).toLong())
                delay(delayMs)
            }
        }
        return Result.failure(Exception("Max reconnection attempts reached"))
    }
}
