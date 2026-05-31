package ru.psychologicalTesting.main.config.authentication

import io.ktor.server.application.Application
import io.ktor.server.config.getAs
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DefaultAuthenticationConfig(
    @Provided app: Application
) : AuthenticationConfig by app.environment.config.property("authentication").getAs<SerialAuthenticationConfig>()
