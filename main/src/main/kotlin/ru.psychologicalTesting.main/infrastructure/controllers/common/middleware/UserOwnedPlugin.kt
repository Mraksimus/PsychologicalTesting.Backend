package ru.psychologicalTesting.main.infrastructure.controllers.common.middleware

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondForbidden
import ru.psychologicalTesting.main.plugins.authentication.UserPrincipal
import ru.psychologicalTesting.main.plugins.suspendedTransaction
import java.util.*

fun createUserOwnedPlugin(
    name: String,
    getOwnerId: (ApplicationCall) -> UUID?
) = createRouteScopedPlugin(name) {

    on(AuthenticationChecked) { call ->

        val userId = call.principal<UserPrincipal>()?.id
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@on
        }

        val ownerId = suspendedTransaction {
            getOwnerId(call)
        }

        if (ownerId == null) {
            call.respond(HttpStatusCode.NotFound)
            return@on
        }

        if (userId != ownerId) {
            call.respondForbidden("Insufficient permissions")
        }

    }

}
