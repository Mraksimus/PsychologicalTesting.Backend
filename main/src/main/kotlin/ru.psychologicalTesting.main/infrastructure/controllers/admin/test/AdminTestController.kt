package ru.psychologicalTesting.main.infrastructure.controllers.admin.test

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
import ru.psychologicalTesting.common.testing.test.ExistingTest
import ru.psychologicalTesting.common.testing.test.NewTest
import ru.psychologicalTesting.main.infrastructure.controllers.admin.test.types.parameters.TestPageParameters
import ru.psychologicalTesting.main.infrastructure.controllers.admin.test.types.parameters.TestScopeParameters
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.requirePermissions
import ru.psychologicalTesting.main.infrastructure.controllers.common.requests.ReorderRequest
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ForbiddenResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.TestDetails
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.repositories.testing.question.QuestionRepository
import ru.psychologicalTesting.main.infrastructure.repositories.testing.test.TestRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Tests (admin)"

fun Routing.configureAdminTestRouting() = route("/admin/tests") {

    authenticate("user") {
        configureAuthenticatedRoutes()
    }

}

private fun Route.configureAuthenticatedRoutes() {

    val testRepository by inject<TestRepository>()
    val questionRepository by inject<QuestionRepository>()

    requirePermissions(Permission.TESTS_VIEW) {

        get(::TestPageParameters) {

            description = "List all tests (paged)"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<PageResponse<ExistingTest>>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {

                val result = suspendedTransaction {
                    testRepository.findAllPage(
                        offset = parameters.offset,
                        limit = parameters.limit
                    )
                }

                call.respond(result)
            }
        }

        get("{test_id}", ::TestScopeParameters) {

            description = "Get a test with its questions"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<TestDetails>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Test does not exist"
                }
            }

            handle {

                val details = suspendedTransaction {
                    val test = testRepository.findOneById(parameters.testId)
                        ?: return@suspendedTransaction null

                    val questions = questionRepository
                        .findAllByTestId(parameters.testId)
                        .sortedBy { it.position }

                    TestDetails(test, questions)
                }

                if (details == null) {
                    call.respondNotFound("Test(id=${parameters.testId}) does not exist")
                } else {
                    call.respond(details)
                }
            }
        }

    }

    requirePermissions(Permission.TESTS_EDIT) {

        post("new") {

            description = "Create a new test"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewTest>()

            responses {
                HttpStatusCode.Created returns typeInfo<ExistingTest>()
                HttpStatusCode.BadRequest returns typeInfo<ProblemDetailsMessage>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {

                val body = call.receive<NewTest>()

                val test = suspendedTransaction {
                    testRepository.create(body)
                }

                call.respond(HttpStatusCode.Created, test)
            }
        }

        patch("{test_id}", ::TestScopeParameters) {

            description = "Update a test"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewTest>()

            responses {
                noContent()
                HttpStatusCode.BadRequest returns typeInfo<ProblemDetailsMessage>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Test does not exist"
                }
            }

            handle {

                val body = call.receive<NewTest>()

                val isUpdated = suspendedTransaction {
                    testRepository.update(
                        id = parameters.testId,
                        dto = body
                    )
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Test(id=${parameters.testId}) does not exist")
                }
            }
        }

        put("{test_id}/position", ::TestScopeParameters) {

            description = "Reorder a test"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<ReorderRequest>()

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Test does not exist"
                }
            }

            handle {

                val body = call.receive<ReorderRequest>()

                val isUpdated = suspendedTransaction {
                    testRepository.updatePositionById(
                        id = parameters.testId,
                        position = body.position
                    )
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Test(id=${parameters.testId}) does not exist")
                }
            }
        }

        delete("{test_id}", ::TestScopeParameters) {

            description = "Delete a test"
            tags = listOf(SWAGGER_TAG)

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Test does not exist"
                }
            }

            handle {

                val isDeleted = suspendedTransaction {
                    testRepository.delete(parameters.testId)
                }

                if (isDeleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Test(id=${parameters.testId}) does not exist")
                }
            }
        }

    }

}
