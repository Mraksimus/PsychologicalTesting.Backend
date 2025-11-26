package ru.psychologicalTesting.main.infrastructure.controllers.common.responses

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class BadRequestResponse(
    val errors: Map<String, String> = emptyMap(),
    val description: String? = null
)

suspend fun ApplicationCall.respondBadRequest(
    description: String
) = respond(
    status = HttpStatusCode.BadRequest,
    message = BadRequestResponse(description = description)
)
