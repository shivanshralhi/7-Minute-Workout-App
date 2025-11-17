package com.plcoding.a7minworkoutapp

class AiCoachRepository(private val api: GeminiApi) {

    suspend fun askAi(prompt: String): String {

        val request = GeminiRequest(
            contents = listOf(
                GeminiMessage(
                    role = "user",
                    parts = listOf(GeminiText(prompt))
                )
            )
        )

        val response = api.getAIResponse(request)

        return response.candidates.firstOrNull()
            ?.content?.parts?.firstOrNull()?.text
            ?: "I’m sorry, I couldn’t generate advice."
    }
}
