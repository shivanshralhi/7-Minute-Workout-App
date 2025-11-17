package com.plcoding.a7minworkoutapp

data class GeminiRequest(
    val contents: List<GeminiMessage>
)
data class GeminiMessage(
    val role: String,
    val parts: List<GeminiText>
)

data class GeminiText(
    val text: String
)
