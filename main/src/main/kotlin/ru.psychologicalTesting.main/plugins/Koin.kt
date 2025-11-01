package ru.psychologicalTesting.main.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import ru.psychologicalTesting.main.config.ConfigModule
import ru.psychologicalTesting.main.infrastructure.repositories.RepositoriesModule
import ru.psychologicalTesting.main.infrastructure.services.ServicesModule
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {

        val applicationModule = module {
            single { this@configureKoin }
        }

        modules(
            applicationModule,
            ConfigModule().module,
            RepositoriesModule().module,
            ServicesModule().module
        )

    }
}
