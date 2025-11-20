package ru.psychologicalTesting.llm.config.ollama

import kotlinx.serialization.Serializable

@Serializable
data class SerialOllamaConfig(
    override val url: String,
    override val model: String,
    override val systemPrompt: String,
) : OllamaConfig
