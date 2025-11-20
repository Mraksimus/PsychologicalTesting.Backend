package ru.psychologicalTesting.main.infrastructure.controllers.authentication

import dev.h4kt.ktorDocs.dsl.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.LoginRequest
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.RegisterRequest
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.responses.AuthenticationResponse
import ru.psychologicalTesting.main.infrastructure.services.authentication.AuthenticationService
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.LoginResult
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.RegistrationResult
import ru.psychologicalTesting.main.plugins.suspendedTransaction

fun Routing.configureAuthenticationRouting() = route("/auth") {
    configurePublicRoutes()
}

private fun Route.configurePublicRoutes() {

    val authenticationService by inject<AuthenticationService>()

    post("register") {

        description = "Register a new account"
        tags = listOf("Authentication")

        requestBody = typeInfo<RegisterRequest>()

        responses {
            HttpStatusCode.OK returns typeInfo<AuthenticationResponse>()
        }

        handle {

            val (
                name,
                surname,
                patronymic,
                email,
                password
            ) = call.receive<RegisterRequest>()

            val result = suspendedTransaction {
                authenticationService.register(
                    name = name,
                    surname = surname,
                    patronymic = patronymic,
                    email = email,
                    password = password
                )
            }

            when (result) {
                is RegistrationResult.Success -> call.respond(result.token)
            }

        }

    }

    post("login") {

        description = "Log into an existing account"
        tags = listOf("Authentication")

        requestBody = typeInfo<LoginRequest>()

        responses {
            HttpStatusCode.OK returns typeInfo<AuthenticationResponse>()
            HttpStatusCode.Unauthorized returns nothing
        }

        handle {

            val (email, password) = call.receive<LoginRequest>()

            val result = suspendedTransaction {
                authenticationService.login(
                    email = email,
                    password = password
                )
            }

            when (result) {
                LoginResult.InvalidCredentials -> call.respond(HttpStatusCode.Unauthorized)
                is LoginResult.Success -> call.respond(result.token)
            }

        }

    }

}
