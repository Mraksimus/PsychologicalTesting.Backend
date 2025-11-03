package ru.psychologicalTesting.llm.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import ru.psychologicalTesting.llm.infrastructure.controllers.configureOllamaRouting

fun Application.configureRouting() = routing {
    configureOllamaRouting()
}
