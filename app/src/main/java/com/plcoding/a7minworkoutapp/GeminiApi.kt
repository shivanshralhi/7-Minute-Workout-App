package com.plcoding.a7minworkoutapp

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface GeminiApi {
    // We use @Url to force the exact link, bypassing any path issues
    @POST
    suspend fun getAIResponse(
        @Url url: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}