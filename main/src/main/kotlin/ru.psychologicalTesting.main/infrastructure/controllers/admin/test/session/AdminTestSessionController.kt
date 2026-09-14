package ru.psychologicalTesting.main.infrastructure.controllers.admin.test.session

import dev.h4kt.ktorDocs.dsl.get
import dev.h4kt.ktorDocs.types.parameters.RouteParameters
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.requirePermissions
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ForbiddenResponse
import ru.psychologicalTesting.main.infrastructure.dto.AdminSessionItem
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.repositories.testing.session.TestingSessionRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Test sessions (admin)"

class AdminTestSessionPageParameters : RouteParameters() {

    val testId by path.uuid {
        name = "test_id"
        description = "Test id"
    }

    val offset by query.long {
        name = "offset"
        description = "Offset of the first item to return"
    }

    val limit by query.int {
        name = "limit"
        description = "Maximum number of items to return"
    }

}

fun Routing.configureAdminTestSessionRouting() = route("/admin/tests/{test_id}/sessions") {

    authenticate("user") {
        configureAuthenticatedRoutes()
    }

}

private fun Route.configureAuthenticatedRoutes() {

    val sessionRepository by inject<TestingSessionRepository>()

    requirePermissions(Permission.SESSIONS_VIEW) {

        get(::AdminTestSessionPageParameters) {

            description = "List sessions of a test (paged) with respondent info"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<PageResponse<AdminSessionItem>>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {

                val result = suspendedTransaction {
                    sessionRepository.findAllAdminByTestIdPaged(
                        testId = parameters.testId,
                        offset = parameters.offset,
                        limit = parameters.limit
                    )
                }

                call.respond(result)
            }
        }

    }

}
