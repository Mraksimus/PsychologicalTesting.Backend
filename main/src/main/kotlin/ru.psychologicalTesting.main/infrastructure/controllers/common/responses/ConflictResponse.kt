package ru.psychologicalTesting.main.infrastructure.controllers.common.responses

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

data class ConflictResponse(
    val errors: Map<String, String> = emptyMap(),
    val description: String? = null
)

suspend fun ApplicationCall.respondConflict(
    description: String
) = respond(
    status = HttpStatusCode.Conflict,
    message = ConflictResponse(description = description)
)
