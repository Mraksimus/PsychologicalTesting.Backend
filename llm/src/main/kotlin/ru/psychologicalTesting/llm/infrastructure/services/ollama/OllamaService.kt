package ru.psychologicalTesting.llm.infrastructure.services.ollama

interface OllamaService {

    suspend fun ask(prompt: String): String

}
