package ru.psychologicalTesting.main.infrastructure.controllers.common.responses

import dev.h4kt.ktorDocs.types.route.RouteResponsesBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.Serializable

@Serializable
data class NotFoundResponse(
    val description: String
)

suspend fun ApplicationCall.respondNotFound(
    description: String
) = respond(HttpStatusCode.NotFound, NotFoundResponse(description))

fun RouteResponsesBuilder.notFound(
    description: String = ""
) {
    HttpStatusCode.NotFound returns {
        body = typeInfo<NotFoundResponse>()
        this.description = description.takeIf { it.isNotEmpty() }
    }
}
