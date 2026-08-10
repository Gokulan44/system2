package com.systemmonitor.domain.model

enum class CommandType {
    LOCK,
    SLEEP,
    RESTART,
    SHUTDOWN
}

data class RemoteCommand(
    val commandId: String,
    val targetDeviceId: String,
    val type: CommandType,
    val pin: String? = null,
    val delaySeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
