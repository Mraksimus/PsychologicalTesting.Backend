package ru.psychologicalTesting.main.config.email

import io.ktor.server.application.Application
import io.ktor.server.config.getAs
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DefaultEmailConfig(
    @Provided app: Application
) : EmailConfig by app.environment.config.property("email").getAs<SerialEmailConfig>()
