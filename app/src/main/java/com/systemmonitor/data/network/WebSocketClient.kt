package com.systemmonitor.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketClient @Inject constructor(
    private val client: OkHttpClient
) {

    private var activeWebSocket: WebSocket? = null

    private val _screenFrameFlow = MutableSharedFlow<Bitmap>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val screenFrameFlow: SharedFlow<Bitmap> = _screenFrameFlow

    private val _connectionStateFlow = MutableSharedFlow<Boolean>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val connectionStateFlow: SharedFlow<Boolean> = _connectionStateFlow

    fun connectScreenStream(wsUrl: String) {
        disconnect()

        val request = Request.Builder().url(wsUrl).build()
        activeWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionStateFlow.tryEmit(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    if (json.optString("type") == "screen_frame") {
                        val base64Img = json.optString("image")
                        if (base64Img.isNotEmpty()) {
                            val decodedBytes = Base64.decode(base64Img, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            if (bitmap != null) {
                                _screenFrameFlow.tryEmit(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionStateFlow.tryEmit(false)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionStateFlow.tryEmit(false)
            }
        })
    }

    fun disconnect() {
        activeWebSocket?.close(1000, "User disconnect")
        activeWebSocket = null
        _connectionStateFlow.tryEmit(false)
    }
}
