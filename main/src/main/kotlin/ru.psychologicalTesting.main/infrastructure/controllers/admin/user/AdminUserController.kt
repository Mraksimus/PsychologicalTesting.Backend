package ru.psychologicalTesting.main.infrastructure.controllers.admin.user

import dev.h4kt.ktorDocs.dsl.get
import dev.h4kt.ktorDocs.dsl.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.admin.user.requests.CreateUserRequest
import ru.psychologicalTesting.main.infrastructure.controllers.admin.user.types.parameters.UserPageParameters
import ru.psychologicalTesting.main.infrastructure.controllers.admin.user.types.parameters.UserScopeParameters
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.requirePermissions
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.BadRequestResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ConflictResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ForbiddenResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondBadRequest
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondConflict
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.dto.user.AdminUserItem
import ru.psychologicalTesting.main.infrastructure.repositories.role.RoleRepository
import ru.psychologicalTesting.main.infrastructure.repositories.user.UserRepository
import ru.psychologicalTesting.main.infrastructure.services.user.UserService
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Users (admin)"

fun Routing.configureAdminUserRouting() = route("/admin/users") {

    authenticate("user") {
        configureAuthenticatedRoutes()
    }

}

private fun Route.configureAuthenticatedRoutes() {

    val userRepository by inject<UserRepository>()
    val userService by inject<UserService>()
    val roleRepository by inject<RoleRepository>()

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

    requirePermissions(Permission.USERS_EDIT) {

        post("new") {

            description = "Create a new user"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<CreateUserRequest>()

            responses {
                HttpStatusCode.Created returns typeInfo<AdminUserItem>()
                HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
                HttpStatusCode.Conflict returns typeInfo<ConflictResponse>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {

                val body = call.receive<CreateUserRequest>()

                val name = body.name.trim()
                val surname = body.surname.trim()
                val patronymic = body.patronymic?.trim()?.takeIf { it.isNotEmpty() }
                val email = body.email.trim()
                val password = body.password

                if (name.isBlank() || surname.isBlank() || email.isBlank() || password.isBlank()) {
                    call.respondBadRequest("Заполните все обязательные поля")
                    return@handle
                }

                val result = suspendedTransaction {
                    val existing = userRepository.findByEmail(email)
                    if (existing != null) {
                        return@suspendedTransaction null
                    }
                    val created = userService.create(
                        name = name,
                        surname = surname,
                        patronymic = patronymic,
                        email = email,
                        password = password,
                    )
                    if (body.roleId != null) {
                        roleRepository.assignToUser(created.id, body.roleId)
                    }
                    userRepository.findAdminItemById(created.id)
                }

                if (result == null) {
                    call.respondConflict("Пользователь с таким email уже существует")
                } else {
                    call.respond(HttpStatusCode.Created, result)
                }
            }
        }
    }

}
