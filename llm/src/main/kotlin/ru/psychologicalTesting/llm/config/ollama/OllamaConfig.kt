package ru.psychologicalTesting.llm.config.ollama

interface OllamaConfig {
    val url: String
    val chatModel: String
    val testTranscriptionModel: String
    val chatSystemPrompt: String
    val testTranscriptionSystemPrompt: String
}
