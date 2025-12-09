package com.plcoding.a7minworkoutapp

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? // <--- Added ?
)

data class GeminiCandidate(
    val content: GeminiContent?            // <--- Added ?
)

data class GeminiContent(
    val parts: List<GeminiPart>?           // <--- Added ?
)

data class GeminiPart(
    val text: String?                      // <--- Added ?
)