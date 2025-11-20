package ru.psychologicalTesting.main.infrastructure.controllers.chat.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String
)