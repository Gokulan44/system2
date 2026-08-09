package com.systemmonitor.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.data.network.ConnectionManager
import com.systemmonitor.data.network.WebSocketClient
import com.systemmonitor.domain.model.Laptop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ScreenViewModel @Inject constructor(
    private val connectionManager: ConnectionManager
) : ViewModel() {

    private val wsClient = WebSocketClient()

    val screenFrame: StateFlow<Bitmap?> = wsClient.screenFrameFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val isConnected: StateFlow<Boolean> = wsClient.connectionStateFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun startStreaming(laptop: Laptop) {
        val url = connectionManager.getWebSocketStreamUrl(laptop)
        wsClient.connectScreenStream(url)
    }

    fun stopStreaming() {
        wsClient.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
    }
}
