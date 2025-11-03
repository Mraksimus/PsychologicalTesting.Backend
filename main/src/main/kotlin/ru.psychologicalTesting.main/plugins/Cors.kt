package ru.psychologicalTesting.main.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.config.cors.CorsConfig
import ru.psychologicalTesting.main.config.cors.configure

fun Application.configureCors() = install(CORS) {
    val config by this@configureCors.inject<CorsConfig>()
    configure(config)
}
