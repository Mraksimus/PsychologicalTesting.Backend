package ru.psychologicalTesting.llm.plugins

import ai.koog.ktor.Koog
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.llm.config.ollama.OllamaConfig

fun Application.configureKoog() = install(Koog) {

    val ollamaConfig by this@configureKoog.inject<OllamaConfig>()

    llm {

        ollama {
            baseUrl = ollamaConfig.url
        }

        fallback {
            provider = LLMProvider.Ollama
            model = LLModel(
                provider = LLMProvider.Ollama,
                id = ollamaConfig.chatModel,
                capabilities = listOf(),
                contextLength = 32_000
            )
        }

    }

    agentConfig {

        prompt(name = "psychologist") {
            system(ollamaConfig.chatSystemPrompt.trimIndent())
        }

        maxAgentIterations = 10

    }

}
