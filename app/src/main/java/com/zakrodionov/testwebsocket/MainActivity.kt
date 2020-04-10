package com.zakrodionov.testwebsocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit
//https://stackoverflow.com/questions/59681449/websocket-reconnecting-issue-when-wifi-turned-off-and-turned-on-again
//remove okHttpClient.dispatcher().executorService().shutdown()
class MainActivity : AppCompatActivity() {

    private val listener = WebSocketEcho()

    private val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    private val request = Request.Builder()
            .url("wss://echo.websocket.org")
            .build()

    var ws: WebSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart.setOnClickListener { connectSocket() }
        btnStop.setOnClickListener { stopSocket() }
        btnSend.setOnClickListener { sendText() }
    }
    
    private fun connectSocket() {
        ws = client.newWebSocket(request, listener)
    }

    private fun stopSocket() {
        ws?.close(4999, "stop")
        //ws?.cancel()
    }

    private fun sendText() {
        ws?.send("Привет")
    }

}
