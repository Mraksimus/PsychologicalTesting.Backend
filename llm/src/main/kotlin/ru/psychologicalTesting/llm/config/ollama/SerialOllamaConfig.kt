package ru.psychologicalTesting.llm.config.ollama

import kotlinx.serialization.Serializable

@Serializable
data class SerialOllamaConfig(
    override val url: String,
    override val chatModel: String,
    override val testTranscriptionModel: String,
    override val chatContext: Long,
    override val testTranscriptionContext: Long,
    override val chatSystemPrompt: String,
    override val testTranscriptionSystemPrompt: String,
    override val testTranscriptionTemperature: Double,
    override val testTranscriptionMaxOutputTokens: Long,
) : OllamaConfig
