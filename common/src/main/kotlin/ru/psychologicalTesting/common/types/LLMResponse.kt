package ru.psychologicalTesting.common.types

import kotlinx.serialization.Serializable

@Serializable
data class LLMResponse(
    val message: String
)
