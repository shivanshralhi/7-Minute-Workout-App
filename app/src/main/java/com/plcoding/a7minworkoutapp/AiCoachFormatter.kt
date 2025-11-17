package com.plcoding.a7minworkoutapp

object AiCoachFormatter {
    fun buildFitnessPrompt(
        pauses: Int,
        quitEarly: Boolean,
        completed: Boolean,
        totalExercises: Int,
        bodyType: String,
        strength: String
    ): String {

        return """
            You are an AI fitness coach.
            Here is the user's last workout session:

            • Pauses: $pauses  
            • Quit Early: $quitEarly  
            • Completed: $completed  
            • Total Exercises: $totalExercises  
            • Body Type: $bodyType  
            • Strength Level: $strength  

            Give:
            1. Friendly motivational feedback  
            2. What they did well  
            3. What they should improve  
            4. Recommended exercise duration and rest changes  
            5. Short diet tip
        """.trimIndent()
    }
}