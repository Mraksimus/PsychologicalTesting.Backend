package ru.psychologicalTesting.llm.infrastructure.controllers

import kotlinx.serialization.Serializable

@Serializable
data class PromptResponse(
    val value: String
)
