package ru.psychologicalTesting.main.infrastructure.controllers.common.middleware

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondForbidden
import ru.psychologicalTesting.main.infrastructure.controllers.common.utils.transparentRoute
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.plugins.authentication.UserPrincipal

class PermissionsPluginConfig {
    var permissions: Array<out Permission> = arrayOf()
}

val PermissionsPlugin = createRouteScopedPlugin(
    name = "permissions",
    createConfiguration = ::PermissionsPluginConfig
) {

    on(AuthenticationChecked) { call ->

        val principal = call.principal<UserPrincipal>()
        if (principal == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@on
        }

        val userPermissions = principal.role?.permissions.orEmpty().toSet()

        if (Permission.ADMIN in userPermissions) {
            return@on
        }

        val missingPermissions = pluginConfig.permissions.filter { it !in userPermissions }
        if (missingPermissions.isEmpty()) {
            return@on
        }

        call.respondForbidden("Insufficient permissions: ${missingPermissions.joinToString()}")

    }

}

@DslMarker
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class MiddlewareDsl

@MiddlewareDsl
fun Route.requirePermissions(
    vararg permissions: Permission,
    build: Route.() -> Unit
) {

    transparentRoute {

        this@transparentRoute.install(PermissionsPlugin) {
            this.permissions = permissions
        }

        this@transparentRoute.build()

    }

}
