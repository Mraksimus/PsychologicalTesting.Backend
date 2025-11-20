package ru.psychologicalTesting.llm.config.ollama

interface OllamaConfig {
    val url: String
    val model: String
    val systemPrompt: String
}
