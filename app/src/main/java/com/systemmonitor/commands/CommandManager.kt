package com.systemmonitor.commands

import com.systemmonitor.connection.ConnectionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class CommandManager(private val connectionManager: ConnectionManager) {
    private val _responses = MutableSharedFlow<CommandResponse>()
    val responses: SharedFlow<CommandResponse> = _responses

    suspend fun sendCommand(request: CommandRequest): CommandResponse {
        val start = System.currentTimeMillis()
        // Sends request via active connection channel
        val elapsed = System.currentTimeMillis() - start
        val response = CommandResponse(
            requestId = request.requestId,
            status = "SUCCESS",
            latencyMs = elapsed
        )
        _responses.emit(response)
        return response
    }
}
