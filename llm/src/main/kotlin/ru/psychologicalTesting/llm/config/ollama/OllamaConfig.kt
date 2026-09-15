package ru.psychologicalTesting.llm.config.ollama

interface OllamaConfig {
    val url: String
    val chatModel: String
    val testTranscriptionModel: String
    val chatContext: Long
    val testTranscriptionContext: Long
    val chatSystemPrompt: String
    val testTranscriptionSystemPrompt: String
    val testTranscriptionTemperature: Double
    val testTranscriptionMaxOutputTokens: Long
}
