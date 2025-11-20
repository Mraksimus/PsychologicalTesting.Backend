package ru.psychologicalTesting.main.infrastructure.services.llm

import ru.psychologicalTesting.main.infrastructure.services.llm.results.PromptResult
import java.util.*

interface LLMService {

    suspend fun sendPrompt(
        userId: UUID,
        prompt: String
    ): PromptResult

}
