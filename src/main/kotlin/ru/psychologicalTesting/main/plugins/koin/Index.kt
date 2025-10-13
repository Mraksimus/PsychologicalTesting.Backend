package ru.psychologicalTesting.main.plugins.koin

import io.ktor.server.application.Application
import io.ktor.server.application.install
import ru.psychologicalTesting.main.infrastructure.repositories.RepositoriesModule
import ru.psychologicalTesting.main.infrastructure.services.ServicesModule
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() = install(Koin) {
    modules(
        RepositoriesModule().module,
        ServicesModule().module
    )
}
