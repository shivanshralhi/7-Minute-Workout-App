package com.plcoding.a7minworkoutapp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AiCoachRepository {

    suspend fun askAi(prompt: String, apiKey: String): String {
        return withContext(Dispatchers.IO) {
            var urlConnection: HttpURLConnection? = null
            try {
                // 1. Groq API Endpoint
                val url = URL("https://api.groq.com/openai/v1/chat/completions")

                // 2. Open Connection
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/json")
                urlConnection.setRequestProperty("Authorization", "Bearer $apiKey") // Groq uses "Bearer" token
                urlConnection.doOutput = true

                // 3. Create JSON Body (OpenAI Format)
                // We use Llama-3-8b because it is fast and free
                val jsonBody = JSONObject().apply {
                    put("model", "llama-3.3-70b-versatile") // Or "llama-3.1-8b-instant"
                    put("messages", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    })
                }

                // 4. Send the Request
                val outputStream = OutputStreamWriter(urlConnection.outputStream)
                outputStream.write(jsonBody.toString())
                outputStream.flush()
                outputStream.close()

                // 5. Check Response
                val responseCode = urlConnection.responseCode
                Log.d("AI_DEBUG", "Response Code: $responseCode")

                if (responseCode == 200) {
                    // Success!
                    val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    // Parse JSON: { "choices": [ { "message": { "content": "..." } } ] }
                    val jsonResponse = JSONObject(response.toString())
                    val choices = jsonResponse.getJSONArray("choices")
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    return@withContext message.getString("content")

                } else {
                    // Failure
                    val errorReader = BufferedReader(InputStreamReader(urlConnection.errorStream))
                    val errorResponse = StringBuilder()
                    var line: String?
                    while (errorReader.readLine().also { line = it } != null) {
                        errorResponse.append(line)
                    }
                    errorReader.close()
                    Log.e("AI_DEBUG", "Groq Error: $errorResponse")
                    return@withContext "Error: $errorResponse"
                }

            } catch (e: Exception) {
                Log.e("AI_DEBUG", "Exception: ${e.message}")
                return@withContext "Failed to connect: ${e.message}"
            } finally {
                urlConnection?.disconnect()
            }
        }
    }
}