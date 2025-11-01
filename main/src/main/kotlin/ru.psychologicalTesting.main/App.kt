package ru.psychologicalTesting.main

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import ru.psychologicalTesting.main.plugins.authentication.configureAuthentication
import ru.psychologicalTesting.main.plugins.configureAkkurate
import ru.psychologicalTesting.main.plugins.configureContentNegotiation
import ru.psychologicalTesting.main.plugins.configureDatabase
import ru.psychologicalTesting.main.plugins.configureDocs
import ru.psychologicalTesting.main.plugins.configureKoin
import ru.psychologicalTesting.main.plugins.configureRouting

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {

    configureKoin()

    configureDatabase()

    configureContentNegotiation()
    configureAkkurate()

    configureAuthentication()
    configureRouting()
    configureDocs()

}
