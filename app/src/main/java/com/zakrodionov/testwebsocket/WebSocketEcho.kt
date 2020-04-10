package com.zakrodionov.testwebsocket

import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketEcho : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("test_socket","onOpen: $response")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("test_socket","onMessage: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d("test_socket","onMessage: " + bytes.hex())
    }

    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String
    ) {
        Log.d("test_socket","CLOSE: $code $reason")
    }

    override fun onFailure(
        webSocket: WebSocket,
        t: Throwable,
        response: Response?
    ) {
        t.printStackTrace()
    }
}