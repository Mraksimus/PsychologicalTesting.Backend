package ru.psychologicalTesting.llm.infrastructure.controllers

import kotlinx.serialization.Serializable

@Serializable
data class PromptRequest(
    val value: String
)
