package com.plcoding.a7minworkoutapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class Activity_Ai_Chat : AppCompatActivity() {
    private lateinit var api: GeminiApi
    private lateinit var repo: AiCoachRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)
        val apiKey = getString(R.string.ApiKey)
        api = GeminiClient.createGeminiService(apiKey)
        repo = AiCoachRepository(api)

        val send = findViewById<Button>(R.id.btnSend)
        val input = findViewById<EditText>(R.id.etMessage)
        val chat = findViewById<LinearLayout>(R.id.chatContainer)

        send.setOnClickListener {
            val text = input.text.toString()
            input.setText("")
            addMessage(chat, text, true)

            lifecycleScope.launch {
                val reply = repo.askAi(text)
                addMessage(chat, reply, false)
            }
        }
    }

    private fun addMessage(container: LinearLayout, msg: String, isUser: Boolean) {
        val tv = TextView(this)
        tv.text = msg
        tv.setPadding(20,20,20,20)

        tv.setBackgroundColor(
            if (isUser) 0xFFE0F7FA.toInt()
            else 0xFFFFF9C4.toInt()
        )

        container.addView(tv)
    }
}