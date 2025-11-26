package ru.psychologicalTesting.main.infrastructure.controllers.common.responses

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class ForbiddenResponse(
    val description: String
)

suspend fun ApplicationCall.respondForbidden(
    description: String
) = respond(
    status = HttpStatusCode.Forbidden,
    message = ForbiddenResponse(description)
)
