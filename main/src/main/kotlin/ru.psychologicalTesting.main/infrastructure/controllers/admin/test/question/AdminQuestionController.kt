package ru.psychologicalTesting.main.infrastructure.controllers.admin.test.question

import dev.h4kt.ktorDocs.dsl.delete
import dev.h4kt.ktorDocs.dsl.get
import dev.h4kt.ktorDocs.dsl.patch
import dev.h4kt.ktorDocs.dsl.post
import dev.h4kt.ktorDocs.dsl.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.common.testing.question.ExistingQuestion
import ru.psychologicalTesting.common.testing.question.NewQuestion
import ru.psychologicalTesting.main.infrastructure.controllers.admin.test.question.requests.NewQuestionRequest
import ru.psychologicalTesting.main.infrastructure.controllers.admin.test.question.types.parameters.QuestionScopeParameters
import ru.psychologicalTesting.main.infrastructure.controllers.admin.test.types.parameters.TestScopeParameters
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.requirePermissions
import ru.psychologicalTesting.main.infrastructure.controllers.common.requests.ReorderRequest
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ForbiddenResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.repositories.testing.question.QuestionRepository
import ru.psychologicalTesting.main.infrastructure.repositories.testing.test.TestRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Test questions (admin)"

fun Routing.configureAdminQuestionRouting() = route("/admin/tests/{test_id}/questions") {

    authenticate("user") {
        configureAuthenticatedRoutes()
    }

}

private fun Route.configureAuthenticatedRoutes() {

    val questionRepository by inject<QuestionRepository>()
    val testRepository by inject<TestRepository>()

    requirePermissions(Permission.TESTS_VIEW) {

        get(::TestScopeParameters) {

            description = "List questions of a test"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<List<ExistingQuestion>>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Test does not exist"
                }
            }

            handle {

                val result = suspendedTransaction {
                    val test = testRepository.findOneById(parameters.testId)
                        ?: return@suspendedTransaction null

                    questionRepository
                        .findAllByTestId(test.id)
                        .sortedBy { it.position }
                }

                if (result == null) {
                    call.respondNotFound("Test(id=${parameters.testId}) does not exist")
                } else {
                    call.respond(result)
                }
            }
        }

    }

    requirePermissions(Permission.QUESTIONS_EDIT) {

        post("new", ::TestScopeParameters) {

            description = "Add a question to a test"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewQuestionRequest>()

            responses {
                HttpStatusCode.Created returns typeInfo<ExistingQuestion>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Test does not exist"
                }
            }

            handle {

                val body = call.receive<NewQuestionRequest>()

                val result = suspendedTransaction {
                    val test = testRepository.findOneById(parameters.testId)
                        ?: return@suspendedTransaction null

                    questionRepository.create(
                        NewQuestion(
                            testId = test.id,
                            content = body.content
                        )
                    )
                }

                if (result == null) {
                    call.respondNotFound("Test(id=${parameters.testId}) does not exist")
                } else {
                    call.respond(HttpStatusCode.Created, result)
                }
            }
        }

        patch("{question_id}", ::QuestionScopeParameters) {

            description = "Update a question"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewQuestionRequest>()

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Question does not exist"
                }
            }

            handle {

                val body = call.receive<NewQuestionRequest>()

                val isUpdated = suspendedTransaction {
                    questionRepository.update(
                        id = parameters.questionId,
                        dto = NewQuestion(
                            testId = parameters.testId,
                            content = body.content
                        )
                    )
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Question(id=${parameters.questionId}) does not exist")
                }
            }
        }

        put("{question_id}/position", ::QuestionScopeParameters) {

            description = "Reorder a question"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<ReorderRequest>()

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Question does not exist"
                }
            }

            handle {

                val body = call.receive<ReorderRequest>()

                val isUpdated = suspendedTransaction {
                    questionRepository.updatePositionById(
                        id = parameters.questionId,
                        position = body.position
                    )
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Question(id=${parameters.questionId}) does not exist")
                }
            }
        }

        delete("{question_id}", ::QuestionScopeParameters) {

            description = "Delete a question"
            tags = listOf(SWAGGER_TAG)

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Question does not exist"
                }
            }

            handle {

                val isDeleted = suspendedTransaction {
                    questionRepository.delete(parameters.questionId)
                }

                if (isDeleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Question(id=${parameters.questionId}) does not exist")
                }
            }
        }

    }

}
