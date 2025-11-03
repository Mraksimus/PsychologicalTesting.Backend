package ru.psychologicalTesting.llm.infrastructure.services.ollama

import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.llm.OllamaModels
import org.koin.core.annotation.Single

@Single
class KoogOllamaService : OllamaService {

    private val model = OllamaModels.Meta.LLAMA_3_2

    private val executor = simpleOllamaAIExecutor()

    override suspend fun ask(prompt: String): String {

        val prompt = prompt(
            existing = Prompt.Empty
        ) {
            user(prompt)
        }

        val responses = executor.execute(
            prompt = prompt,
            model = model,
            tools = emptyList()
        )

        return responses.joinToString("\n") {
            it.content
        }
    }

}
