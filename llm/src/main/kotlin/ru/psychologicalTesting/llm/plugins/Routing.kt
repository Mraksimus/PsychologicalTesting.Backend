package ru.psychologicalTesting.llm.plugins

import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing
import ru.psychologicalTesting.llm.infrastructure.controllers.ollama.configureOllamaRouting

fun Application.configureRouting() = routing {

    swaggerUI(path = "/docs", swaggerFile = "api.json")

    configureOllamaRouting()

}
