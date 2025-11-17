package com.plcoding.a7minworkoutapp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    fun createGeminiService(apiKey: String): GeminiApi {

        val interceptor = Interceptor { chain ->
            val oldRequest = chain.request()
            val oldUrl = oldRequest.url()

            val newUrl = oldUrl.newBuilder()
                .addQueryParameter("key", apiKey)
                .build()

            val newRequest = oldRequest.newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(newRequest)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

}