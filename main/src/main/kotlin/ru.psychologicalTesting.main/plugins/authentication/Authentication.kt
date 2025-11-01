package ru.psychologicalTesting.main.plugins.authentication

import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.bearer
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.services.authentication.AuthenticationService
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.TokenValidationResult

fun Application.configureAuthentication() = authentication {

    val authenticationService by this@configureAuthentication.inject<AuthenticationService>()

    bearer("user") {
        authenticate {

            val result = authenticationService.validateToken(it.token)
            if (result !is TokenValidationResult.Success) {
                return@authenticate null
            }

            val (userId) = result

            return@authenticate UserPrincipal(id = userId)
        }
    }

}
