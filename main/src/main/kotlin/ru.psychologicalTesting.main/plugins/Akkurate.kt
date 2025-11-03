package ru.psychologicalTesting.main.plugins

import dev.nesk.akkurate.ktor.server.Akkurate
import dev.nesk.akkurate.ktor.server.registerValidator
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import org.koin.ktor.ext.getKoin
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.LoginRequest
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.RegisterRequest
import ru.psychologicalTesting.main.infrastructure.repositories.user.UserRepository

fun Application.configureAkkurate() {

    val koin = getKoin()

    install(Akkurate)

    install(RequestValidation) {
        registerValidator(LoginRequest.validator)
        registerValidator(RegisterRequest.validator(koin.get<UserRepository>()))
    }

}
