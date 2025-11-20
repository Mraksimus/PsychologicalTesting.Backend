package ru.psychologicalTesting.common.types

import kotlinx.serialization.Serializable

@Serializable
data class LLMChatResponse(
    val message: String
)
