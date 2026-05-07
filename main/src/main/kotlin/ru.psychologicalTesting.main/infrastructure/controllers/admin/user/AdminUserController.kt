package ru.psychologicalTesting.main.infrastructure.controllers.admin.user

import dev.h4kt.ktorDocs.dsl.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.admin.user.types.parameters.UserPageParameters
import ru.psychologicalTesting.main.infrastructure.controllers.admin.user.types.parameters.UserScopeParameters
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.requirePermissions
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ForbiddenResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.dto.user.AdminUserItem
import ru.psychologicalTesting.main.infrastructure.repositories.user.UserRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Users (admin)"

fun Routing.configureAdminUserRouting() = route("/admin/users") {

    authenticate("user") {
        configureAuthenticatedRoutes()
    }

}

private fun Route.configureAuthenticatedRoutes() {

    val userRepository by inject<UserRepository>()

    requirePermissions(Permission.USERS_VIEW) {

        get(::UserPageParameters) {

            description = "List all users (paged) with role and optional filters"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<PageResponse<AdminUserItem>>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {

                val result = suspendedTransaction {
                    userRepository.findAllAdminPaged(
                        offset = parameters.offset,
                        limit = parameters.limit,
                        search = parameters.search,
                        roleId = parameters.roleId
                    )
                }

                call.respond(result)
            }
        }

        get("{user_id}", ::UserScopeParameters) {

            description = "Get a user by id with role information"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<AdminUserItem>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "User does not exist"
                }
            }

            handle {

                val user = suspendedTransaction {
                    userRepository.findAdminItemById(parameters.userId)
                }

                if (user == null) {
                    call.respondNotFound("User(id=${parameters.userId}) does not exist")
                } else {
                    call.respond(user)
                }
            }
        }

    }

}
