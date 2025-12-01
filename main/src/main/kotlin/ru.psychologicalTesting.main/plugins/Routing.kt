package ru.psychologicalTesting.main.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.configureAuthenticationRouting
import ru.psychologicalTesting.main.infrastructure.controllers.chat.configureChatRouting
import ru.psychologicalTesting.main.infrastructure.controllers.profile.conffigureUserProfileRouting
import ru.psychologicalTesting.main.infrastructure.controllers.testing.session.configureTestingSessionRouting
import ru.psychologicalTesting.main.infrastructure.controllers.testing.test.configureTestRouting

fun Application.configureRouting() = routing {
    configureAuthenticationRouting()
    configureChatRouting()
    configureTestRouting()
    configureTestingSessionRouting()
    conffigureUserProfileRouting()
}
