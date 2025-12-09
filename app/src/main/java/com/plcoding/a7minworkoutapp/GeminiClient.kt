package com.plcoding.a7minworkoutapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiClient {
    // This base URL is just a placeholder now because we use @Url override
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    fun createGeminiService(): GeminiApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}