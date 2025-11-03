package ru.psychologicalTesting.llm.infrastructure.controllers

import dev.h4kt.ktorDocs.dsl.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.llm.infrastructure.services.ollama.OllamaService

fun Routing.configureOllamaRouting() = route("/ollama") {

    configurePublicRoutes()

}

private fun Route.configurePublicRoutes() {

    val ollamaService by inject<OllamaService>()

    post("prompt") {

        description = "Ask to llama"
        tags = listOf("Ollama")

        requestBody = typeInfo<PromptRequest>()

        responses {
            HttpStatusCode.OK returns typeInfo<PromptResponse>()
        }

        handle {

            val (value) = call.receive<PromptRequest>()

            val result = ollamaService.ask(value)

            call.respond(HttpStatusCode.OK, result)
        }

    }

}
