package ru.psychologicalTesting.llm.config.cors

import io.ktor.server.application.Application
import io.ktor.server.config.getAs
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DefaultCorsConfig(
    @Provided app: Application
) : CorsConfig by app.environment.config.property("cors").getAs<SerialCorsConfig>()
