package com.systemmonitor.commands

import java.util.UUID

data class CommandRequest(
    val type: String,
    val params: Map<String, Any> = emptyMap(),
    val targetDeviceId: String,
    val requestId: String = UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis()
)
