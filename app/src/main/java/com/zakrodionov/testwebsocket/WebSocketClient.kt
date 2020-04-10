package com.zakrodionov.testwebsocket

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

//listener, log для теста, не использовать при копипасте
//Отображает текст в текствью
//Можно улучшить работу с потоками, но вроде и так норм
//удалить коммент при копипасте
class WebSocketClient(
    context: Context,
    url: String = SOCKET_URL,
    val listener: (String) -> Unit //todo delete
) : WebSocketListener() {

    //delete on prod todo
    private fun log(str: String) {
        Log.d(TAG, str)
        socketScope.launch {
            listener.invoke(str)
        }
    }

    companion object {
        private const val SOCKET_URL = "wss://echo.websocket.org"
        private const val RETRY_COUNT = 20
        private const val RETRY_INTERVAL_MS = 1_000L
        private const val TAG = "test_web_socket"
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val request = Request.Builder()
        .url(url)
        .build()

    private var job: Job = SupervisorJob()
    private val socketScope by lazy { CoroutineScope(Dispatchers.Main) }

    private var ws: WebSocket? = null

    private var currentRetryCount = 0
    private var connecting = false
    private var connected = false
        set(connected) {
            connecting = false

            if (connected) {
                currentRetryCount = 0
            }
            field = connected
        }

    init {
        subscribeToConnectivityChange(context)
    }

    private fun subscribeToConnectivityChange(applicationContext: Context?) {
        val intentFilter = IntentFilter()
            .apply { addAction(ConnectivityManager.CONNECTIVITY_ACTION) }
        applicationContext?.registerReceiver(ConnectivityChangeBroadcastReceiver(), intentFilter)
    }

    fun connectWithRetry() {
        currentRetryCount = 0
        job = socketScope.launch {
            while (!connected && !connecting && currentRetryCount <= RETRY_COUNT) {
                log("ATTEMPT_COUNT: $currentRetryCount")
                log("CONNECTED_STATUS: $connected")
                currentRetryCount++
                connectSocket()
                delay(RETRY_INTERVAL_MS)
            }
        }
    }

    private fun connectSocket() {
        if (!connected && !connecting) {
            ws = client.newWebSocket(request, this)
            log("connectSocket: $ws")
            connecting = true
        }
    }

    fun stopSocket() {
        job.cancel()
        ws?.close(4999, "stop") //todo code??
    }

    fun sendText() {
        ws?.send("Привет")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        connected = true
        log("onOpen: $response")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        log("onMessage: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        log("onMessage: " + bytes.hex())
    }

    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String
    ) {
        connected = false
        log("CLOSE: $code $reason")
    }

    override fun onFailure(
        webSocket: WebSocket,
        t: Throwable,
        response: Response?
    ) {
        connected = false
        log("onFailure: $t")
    }

    private inner class ConnectivityChangeBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras ?: return
            val isConnected = !extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY)

            if (isConnected) {
                connectWithRetry()
            }
        }
    }
}