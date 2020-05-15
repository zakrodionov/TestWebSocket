package com.zakrodionov.testwebsocket

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

//https://stackoverflow.com/questions/59681449/websocket-reconnecting-issue-when-wifi-turned-off-and-turned-on-again
//remove okHttpClient.dispatcher().executorService().shutdown()
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //для теста
        val ws = WebSocketClient(this) {
            tvTitle.text = "${tvTitle.text} \n $it \n"
        }

        btnStart.setOnClickListener { ws.connectWithRetry() }
        btnStop.setOnClickListener { ws.stopSocket() }
        btnSend.setOnClickListener { ws.sendText("Hello") }
        btnClear.setOnClickListener { tvTitle.text = "" }
    }

}

inline fun runnable(crossinline func: (Runnable) -> Unit) = object : Runnable {
    override fun run() {
        func.invoke(this)
    }
}

inline fun Handler.postDelay(
    delayMillis: Long,
    crossinline func: () -> Unit
) = postDelayed({ func.invoke() }, delayMillis)
