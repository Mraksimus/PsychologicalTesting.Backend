package ru.psychologicalTesting.main.plugins

import io.ktor.server.application.Application
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authentication
import io.ktor.server.auth.bearer

fun Application.configureAuthentication() = authentication {

    bearer("template") {
        authenticate {
            UserIdPrincipal(it.token)
        }
    }

}
