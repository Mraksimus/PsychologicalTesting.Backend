package ru.psychologicalTesting.main.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import ru.psychologicalTesting.main.infrastructure.controllers.configureTestRouting

fun Application.configureRouting() = routing {
    configureTestRouting()
}
