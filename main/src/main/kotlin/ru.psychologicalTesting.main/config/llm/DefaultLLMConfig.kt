package ru.psychologicalTesting.main.config.llm

import io.ktor.server.application.Application
import io.ktor.server.config.getAs
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DefaultLLMConfig(
    @Provided app: Application
) : LLMConfig by app.environment.config.property("llm").getAs<SerialLLMConfig>()
