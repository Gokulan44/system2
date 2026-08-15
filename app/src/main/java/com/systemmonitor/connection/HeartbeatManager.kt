package com.systemmonitor.connection

import kotlinx.coroutines.*

class HeartbeatManager(
    private val intervalMs: Long = 30000L,
    private val onPing: suspend () -> Unit
) {
    private var job: Job? = null

    fun start() {
        stop()
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                onPing()
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
