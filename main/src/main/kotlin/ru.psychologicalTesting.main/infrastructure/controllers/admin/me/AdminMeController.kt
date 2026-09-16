package ru.psychologicalTesting.main.infrastructure.controllers.admin.me

import dev.h4kt.ktorDocs.dsl.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondForbidden
import ru.psychologicalTesting.main.infrastructure.dto.role.ExistingRole
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.plugins.authentication.UserPrincipal

private const val SWAGGER_TAG = "Admin auth"

@Serializable
data class AdminMeResponse(
    val role: ExistingRole?,
    val permissions: List<Permission>
)

fun Routing.configureAdminMeRouting() = route("/admin/me") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    get {

        description = "Return current user's admin role and permissions. 403 if user has no admin access."
        tags = listOf(SWAGGER_TAG)

        responses {
            HttpStatusCode.OK returns typeInfo<AdminMeResponse>()
            HttpStatusCode.Forbidden returns nothing
        }

        handle {

            val principal = call.principal<UserPrincipal>()!!
            val perms = principal.role?.permissions.orEmpty()

            if (perms.isEmpty()) {
                call.respondForbidden("No admin permissions")
                return@handle
            }

            call.respond(
                HttpStatusCode.OK,
                AdminMeResponse(
                    role = principal.role,
                    permissions = perms
                )
            )
        }

    }

}
