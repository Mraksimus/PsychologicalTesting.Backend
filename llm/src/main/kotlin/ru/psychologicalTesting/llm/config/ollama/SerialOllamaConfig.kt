package ru.psychologicalTesting.llm.config.ollama

import kotlinx.serialization.Serializable

@Serializable
data class SerialOllamaConfig(
    override val url: String,
    override val chatModel: String,
    override val testTranscriptionModel: String,
    override val chatSystemPrompt: String,
    override val testTranscriptionSystemPrompt: String
) : OllamaConfig
