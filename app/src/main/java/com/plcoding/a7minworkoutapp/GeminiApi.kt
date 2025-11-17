package com.plcoding.a7minworkoutapp
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeminiApi {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun getAIResponse(@Body request: GeminiRequest): GeminiResponse
}