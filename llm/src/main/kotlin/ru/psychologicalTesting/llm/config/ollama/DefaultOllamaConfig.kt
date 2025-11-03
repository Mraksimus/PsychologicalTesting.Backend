package ru.psychologicalTesting.llm.config.ollama

import io.ktor.server.application.Application
import io.ktor.server.config.getAs
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DefaultOllamaConfig(
    @Provided app: Application
) : OllamaConfig by app.environment.config.property("environment").getAs<SerialOllamaConfig>()
