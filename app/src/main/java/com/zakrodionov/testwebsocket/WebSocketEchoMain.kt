package com.zakrodionov.testwebsocket

import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketEchoMain : WebSocketListener() {
    private fun run() {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder()
            .url("ws://echo.websocket.org")
            .build()
        client.newWebSocket(request, this)

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher.executorService.shutdown()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("Hello...")
        webSocket.send("...World!")
        webSocket.close(1000, "Goodbye, World!")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("test","MESSAGE: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("test","MESSAGE: " + bytes.hex())
    }

    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String
    ) {
        webSocket.close(1000, null)
        Log.d("test","CLOSE: $code $reason")
    }

    override fun onFailure(
        webSocket: WebSocket,
        t: Throwable,
        response: Response?
    ) {
        t.printStackTrace()
    }
}