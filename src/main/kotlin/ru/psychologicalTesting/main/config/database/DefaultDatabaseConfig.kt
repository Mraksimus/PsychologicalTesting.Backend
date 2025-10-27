package ru.psychologicalTesting.main.config.database

import io.ktor.server.application.Application
import io.ktor.server.config.getAs
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DefaultDatabaseConfig(
    @Provided app: Application
) : DatabaseConfig by app.environment.config.property("database").getAs<SerialDatabaseConfig>()
