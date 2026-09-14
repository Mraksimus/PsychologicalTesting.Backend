package ru.psychologicalTesting.main.infrastructure.controllers.admin.role

import dev.h4kt.ktorDocs.dsl.delete
import dev.h4kt.ktorDocs.dsl.get
import dev.h4kt.ktorDocs.dsl.patch
import dev.h4kt.ktorDocs.dsl.post
import dev.h4kt.ktorDocs.dsl.put
import dev.nesk.akkurate.ktor.server.ProblemDetailsMessage
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.admin.role.requests.AssignRoleRequest
import ru.psychologicalTesting.main.infrastructure.controllers.admin.role.types.parameters.RoleScopeParameters
import ru.psychologicalTesting.main.infrastructure.controllers.admin.role.types.parameters.UserRoleScopeParameters
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.requirePermissions
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ForbiddenResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ConflictResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondConflict
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.dto.role.ExistingRole
import ru.psychologicalTesting.main.infrastructure.dto.role.NewRole
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.repositories.role.RoleRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Roles (admin)"

fun Routing.configureAdminRoleRouting() = route("/admin/roles") {

    authenticate("user") {
        configureAuthenticatedRoutes()
    }

}

private fun Route.configureAuthenticatedRoutes() {

    val roleRepository by inject<RoleRepository>()

    requirePermissions(Permission.ROLES_VIEW) {

        get {

            description = "List all roles"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<List<ExistingRole>>()
            }

            handle {

                val roles = suspendedTransaction {
                    roleRepository.findAll()
                }

                call.respond(roles)
            }
        }

    }

    requirePermissions(Permission.ROLES_EDIT) {

        post("new") {

            description = "Create new role"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewRole>()

            responses {
                HttpStatusCode.Created returns typeInfo<ExistingRole>()
                HttpStatusCode.BadRequest returns typeInfo<ProblemDetailsMessage>()
                HttpStatusCode.Conflict returns typeInfo<ConflictResponse>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {

                val body = call.receive<NewRole>()

                val nameTaken = suspendedTransaction {
                    roleRepository.existsByName(body.name)
                }

                if (nameTaken) {
                    call.respondConflict("Role with name ${body.name} already exists")
                    return@handle
                }

                val role = suspendedTransaction {
                    roleRepository.create(body)
                }

                call.respond(HttpStatusCode.Created, role)
            }
        }

        patch("{role_id}", ::RoleScopeParameters) {

            description = "Update role"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewRole>()

            responses {
                noContent()
                HttpStatusCode.BadRequest returns typeInfo<ProblemDetailsMessage>()
                HttpStatusCode.Conflict returns typeInfo<ConflictResponse>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Role does not exist"
                }
            }

            handle {

                val body = call.receive<NewRole>()

                val nameTaken = suspendedTransaction {
                    roleRepository.existsByNameExcludingId(
                        name = body.name,
                        excludingId = parameters.roleId
                    )
                }

                if (nameTaken) {
                    call.respondConflict("Role with name ${body.name} already exists")
                    return@handle
                }

                val isUpdated = suspendedTransaction {
                    roleRepository.updateById(
                        roleId = parameters.roleId,
                        role = body
                    )
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Role(id=${parameters.roleId}) does not exist")
                }
            }
        }

        put("users/{user_id}", ::UserRoleScopeParameters) {

            description = "Assign a role to user (or unassign by passing null)"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<AssignRoleRequest>()

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "User does not exist"
                }
            }

            handle {

                val body = call.receive<AssignRoleRequest>()

                val isUpdated = suspendedTransaction {
                    roleRepository.assignToUser(
                        userId = parameters.userId,
                        roleId = body.roleId
                    )
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("User(id=${parameters.userId}) does not exist")
                }
            }
        }

        delete("{role_id}", ::RoleScopeParameters) {

            description = "Delete role"
            tags = listOf(SWAGGER_TAG)

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Role does not exist"
                }
            }

            handle {

                val isDeleted = suspendedTransaction {
                    roleRepository.deleteById(parameters.roleId)
                }

                if (isDeleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Role(id=${parameters.roleId}) does not exist")
                }
            }
        }

    }

}
