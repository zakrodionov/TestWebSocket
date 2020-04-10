package com.zakrodionov.testwebsocket

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.internal.ws.RealWebSocket
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketClient(context: Context, url: String = SOCKET_URL) : WebSocketListener() {

    companion object {
        private const val SOCKET_URL = "wss://echo.websocket.org"
        private const val REPEAT_COUNT = 20
        private const val REPEAT_INTERVAL_MS = 1_000L
        private const val TAG = "test_web_socket"
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val request = Request.Builder()
        .url(url)
        .build()

    private var job: Job = SupervisorJob()
    private val socketScope by lazy { CoroutineScope(job + Dispatchers.Main) }

    private var ws: WebSocket? = null

    private var connecting = false
    private var connectingAttempt = 0

    private var connected = false
        set(value) {
            connecting = false

            if (value){
                connectingAttempt = 0
            }
            field = value
        }

    init {
        subscribeToConnectivityChange(context)
    }

    private fun subscribeToConnectivityChange(applicationContext: Context?) {
        val intentFilter = IntentFilter()
            .apply { addAction(ConnectivityManager.CONNECTIVITY_ACTION) }
        applicationContext?.registerReceiver(ConnectivityChangeBroadcastReceiver(), intentFilter)
    }

    fun connectWithRepeat() {
        job = socketScope.launch {
            while(!connected && !connecting && connectingAttempt < REPEAT_COUNT) {
                Log.d(TAG, "REPEAT_COUNT: $connected")
                connectingAttempt--
                connectSocket()
                delay(REPEAT_INTERVAL_MS)
            }
        }
    }

    private fun connectSocket() {
        if (!connected && !connecting) {
            ws = null
            ws = client.newWebSocket(request, this)
            Log.d(TAG, "connectSocket: $ws")
            connecting = true
        }
    }

    fun stopSocket() {
        job.cancel()
        ws?.close(4999, "stop")
        ws = null
    }

    fun sendText() {
        ws?.send("Привет")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        connected = true
        Log.d(TAG, "onOpen: $response")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "onMessage: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d(TAG, "onMessage: " + bytes.hex())
    }

    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String
    ) {
        connected = false
        Log.d(TAG, "CLOSE: $code $reason")
    }

    override fun onFailure(
        webSocket: WebSocket,
        t: Throwable,
        response: Response?
    ) {
        connected = false
        Log.d(TAG, "onFailure: $t")
    }

    private inner class ConnectivityChangeBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras ?: return
            val isConnected = !extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY)

            if (isConnected) {
                connectWithRepeat()
            }
        }
    }
}