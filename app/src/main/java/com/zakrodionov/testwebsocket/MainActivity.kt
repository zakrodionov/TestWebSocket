package com.zakrodionov.testwebsocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

//https://stackoverflow.com/questions/59681449/websocket-reconnecting-issue-when-wifi-turned-off-and-turned-on-again
//remove okHttpClient.dispatcher().executorService().shutdown()
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val ws = WebSocketClient(this)

        //для теста
        val ws = WebSocketClientTestShow(this){
            runOnUiThread {
                tvTitle.text = "${tvTitle.text} \n $it \n"
            }
        }

        btnStart.setOnClickListener { ws.connectWithRepeat() }
        btnStop.setOnClickListener { ws.stopSocket() }
        btnSend.setOnClickListener { ws.sendText() }
    }

}
