package ru.psychologicalTesting.common.types

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.messages.LLMMessage

@Serializable
data class LLMChatRequest(
    val prompt: String,
    val messages: List<LLMMessage>
)
