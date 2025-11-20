package ru.psychologicalTesting.main.infrastructure.services.llm.results

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.types.LLMChatResponse

sealed class PromptResult {

    data object Error : PromptResult()

    @Serializable
    data class Success(
        val llmChatResponse: LLMChatResponse
    ) : PromptResult()

}
