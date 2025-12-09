package com.plcoding.a7minworkoutapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class Activity_Ai_Chat : AppCompatActivity() {

    private lateinit var repo: AiCoachRepository

    // REPLACE WITH YOUR GROQ KEY
    private val myApiKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)

        repo = AiCoachRepository()

        val send = findViewById<ImageButton>(R.id.btnSend)
        val input = findViewById<EditText>(R.id.etMessage)
        val chatContainer = findViewById<LinearLayout>(R.id.chatContainer)

        send.setOnClickListener {
            val userQuestion = input.text.toString().trim()

            if (userQuestion.isEmpty()) {
                return@setOnClickListener
            }

            input.setText("")
            addMessage(chatContainer, userQuestion, true)

            // 1. Get the User's Data (History/Context)
            val contextData = getSystemContext()

            // 2. Combine Context + Question
            // The AI sees the data first, then answers your question based on it.
            val fullPrompt = """
                $contextData
                
                User Question: $userQuestion
            """.trimIndent()

            lifecycleScope.launch {
                try {
                    val reply = repo.askAi(fullPrompt, myApiKey)
                    addMessage(chatContainer, reply, false)

                } catch (e: Exception) {
                    Log.e("AIChat", "API Call Failed", e)
                    addMessage(chatContainer, "Error: ${e.localizedMessage}", false)
                }
            }
        }
    }

    /**
     * This function reads the saved data from your phone
     * and formats it into a string for the AI.
     */
    private fun getSystemContext(): String {
        // Read User Profile
        val userPrefs = getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
        val bodyType = userPrefs.getString("BODY_TYPE", "Average")
        val strength = userPrefs.getString("STRENGTH_LEVEL", "Beginner")

        // Read Last Workout Performance
        val perfPrefs = getSharedPreferences("performance_prefs", Context.MODE_PRIVATE)
        val pauses = perfPrefs.getInt("pauses", 0)
        val quitEarly = perfPrefs.getBoolean("quit_early", false)
        val completed = perfPrefs.getBoolean("completed", false)

        return """
            You are a helpful fitness coach. 
            Here is the user's profile and latest workout stats:
            - Body Type: $bodyType
            - Strength Level: $strength
            - Last Workout Status: ${if (completed) "Completed" else "Incomplete"}
            - Quit Early: $quitEarly
            - Number of Pauses: $pauses
            
            Answer the following question keeping these stats in mind. Keep advice short and motivating.
        """.trimIndent()
    }

    private fun addMessage(container: LinearLayout, msg: String, isUser: Boolean) {
        val tv = TextView(this)
        tv.text = msg
        tv.textSize = 16f
        tv.setPadding(30, 20, 30, 20)

        if (isUser) {
            tv.setBackgroundColor(0xFFE0F7FA.toInt())
            tv.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        } else {
            tv.setBackgroundColor(0xFFFFF9C4.toInt())
            tv.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 8, 0, 8)
        tv.layoutParams = params

        container.addView(tv)
    }
}