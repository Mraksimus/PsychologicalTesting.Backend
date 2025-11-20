package ru.psychologicalTesting.llm

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import ru.psychologicalTesting.llm.plugins.configureContentNegotiation
import ru.psychologicalTesting.llm.plugins.configureCors
import ru.psychologicalTesting.llm.plugins.configureKoin
import ru.psychologicalTesting.llm.plugins.configureKoog
import ru.psychologicalTesting.llm.plugins.configureRouting

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {

    configureKoin()

    configureContentNegotiation()

    configureRouting()

    configureCors()

    configureKoog()

}
