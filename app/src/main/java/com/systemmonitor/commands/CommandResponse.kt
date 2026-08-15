package com.systemmonitor.commands

data class CommandResponse(
    val requestId: String,
    val status: String,
    val payload: Any? = null,
    val errorMessage: String = "",
    val latencyMs: Long = 0L
)
