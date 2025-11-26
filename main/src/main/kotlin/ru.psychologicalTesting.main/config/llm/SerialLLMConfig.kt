package ru.psychologicalTesting.main.config.llm

import kotlinx.serialization.Serializable

@Serializable
data class SerialLLMConfig(
    override val host: String,
    override val port: UShort
) : LLMConfig
