package ru.psychologicalTesting.llm.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import ru.psychologicalTesting.llm.config.ConfigModule

fun Application.configureKoin() {
    install(Koin) {

        val applicationModule = module {
            single { this@configureKoin }
        }

        modules(
            applicationModule,
            ConfigModule().module
        )

    }
}
