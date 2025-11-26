package ru.psychologicalTesting.main.infrastructure.controllers.testing.session

import dev.h4kt.ktorDocs.dsl.get
import dev.h4kt.ktorDocs.dsl.post
import dev.h4kt.ktorDocs.dsl.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.common.parameters.PageParameters
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.BadRequestResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ConflictResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondBadRequest
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondConflict
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.controllers.common.utils.transparentRoute
import ru.psychologicalTesting.main.infrastructure.controllers.testing.session.middleware.TestingSessionOwnershipPlugin
import ru.psychologicalTesting.main.infrastructure.controllers.testing.session.requests.UpdateAnswersSessionRequest
import ru.psychologicalTesting.main.infrastructure.controllers.testing.session.types.parameters.CreateSessionParameters
import ru.psychologicalTesting.main.infrastructure.controllers.testing.session.types.parameters.TestingSessionScopeParameters
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.testing.session.ExistingTestingSession
import ru.psychologicalTesting.main.infrastructure.repositories.testing.session.TestingSessionRepository
import ru.psychologicalTesting.main.infrastructure.services.testing.TestingService
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CloseSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CompleteSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CreateSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.UpdateAnswersResult
import ru.psychologicalTesting.main.plugins.authentication.UserPrincipal
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Testing sessions"

fun Routing.configureTestingSessionRouting() = route("/testing/sessions") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    val testingSessionRepository by inject<TestingSessionRepository>()
    val testingService by inject<TestingService>()

    post("/{test_id}", ::CreateSessionParameters) {

        description = "Create a new testing session"
        tags = listOf(SWAGGER_TAG)

        responses {
            HttpStatusCode.OK returns typeInfo<ExistingTestingSession>()
            HttpStatusCode.Conflict returns typeInfo<ConflictResponse>()
            HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
            HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
        }

        handle {

            val (userId) = call.principal<UserPrincipal>()!!

            val result = suspendedTransaction {
                testingService.createSession(
                    userId = userId,
                    testId = parameters.testId
                )
            }

            when (result) {
                is CreateSessionResult.TestNotFound ->
                    call.respondNotFound("Test (id=${parameters.testId}) does not exist)")
                is CreateSessionResult.TestAlreadyStarted ->
                    call.respondConflict("Testing session with test(id=${parameters.testId}) already started")
                is CreateSessionResult.Success ->
                    call.respond(result.createdSession)
            }
        }

    }

    get(::PageParameters) {

        description = "Get all user sessions"
        tags = listOf(SWAGGER_TAG)

        responses {
            HttpStatusCode.OK returns typeInfo<PageResponse<ExistingTestingSession>>()
            HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
        }

        handle {

            val (userId) = call.principal<UserPrincipal>()!!

            val result = suspendedTransaction {
                testingSessionRepository.findAllByUserIdPaged(
                    userId = userId,
                    offset = parameters.offset,
                    limit = parameters.limit
                )
            }

            call.respond(result)
        }

    }

    route("/{session_id}") {

        transparentRoute {

            install(TestingSessionOwnershipPlugin)

            get(::TestingSessionScopeParameters) {

                description = "Get user session"
                tags = listOf(SWAGGER_TAG)

                responses {
                    HttpStatusCode.OK returns typeInfo<ExistingTestingSession>()
                    HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
                }

                handle {

                    val result = suspendedTransaction {
                        testingSessionRepository.findOneById(parameters.sessionId)
                    }

                    if (result == null) {
                        call.respondNotFound("Session(id=${parameters.sessionId}) does not exist)")
                    } else {
                        call.respond(result)
                    }
                }

            }

            put("/answers", ::TestingSessionScopeParameters) {

                description = "Update session answers"
                tags = listOf(SWAGGER_TAG)

                requestBody = typeInfo<UpdateAnswersSessionRequest>()

                responses {
                    HttpStatusCode.OK returns nothing
                    HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
                    HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
                }

                handle {

                    val (questionResponses) = call.receive<UpdateAnswersSessionRequest>()

                    val result = suspendedTransaction {
                        testingService.updateAnswers(
                            sessionId = parameters.sessionId,
                            questionResponses = questionResponses
                        )
                    }

                    when (result) {
                        is UpdateAnswersResult.SessionNotFound ->
                            call.respondNotFound("Session(id=${parameters.sessionId}) does not exist)")
                        is UpdateAnswersResult.SessionClosed ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) closed")
                        is UpdateAnswersResult.AnswerWasNotUpdated ->
                            call.respond(
                                status = HttpStatusCode.InternalServerError,
                                message = "Session(id=${parameters.sessionId}) not updated"
                            )
                        is UpdateAnswersResult.Success ->
                            call.respond(HttpStatusCode.OK)
                    }
                }

            }

            put("/complete", ::TestingSessionScopeParameters) {

                description = "Complete session"
                tags = listOf(SWAGGER_TAG)

                responses {
                    HttpStatusCode.OK returns typeInfo<ExistingTestingSession>()
                    HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
                    HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
                }

                handle {

                    val result = suspendedTransaction {
                        testingService.completeSession(parameters.sessionId)
                    }

                    when (result) {
                        is CompleteSessionResult.SessionNotFound ->
                            call.respondNotFound("Session(id=${parameters.sessionId}) does not exist")
                        is CompleteSessionResult.SessionMustBeOpened ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) must be opened")
                        is CompleteSessionResult.TestIsNotCompleted ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) is not completed")
                        is CompleteSessionResult.Success ->
                            call.respond(HttpStatusCode.OK, result)
                    }
                }

            }

            put("/close", ::TestingSessionScopeParameters) {

                description = "Close session"
                tags = listOf(SWAGGER_TAG)

                responses {
                    HttpStatusCode.OK returns nothing
                    HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
                    HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
                }

                handle {

                    val result = suspendedTransaction {
                        testingService.closeSession(parameters.sessionId)
                    }

                    when (result) {
                        is CloseSessionResult.SessionMustBeOpened ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) must be opened")
                        is CloseSessionResult.SessionNotFound ->
                            call.respondNotFound("Session(id=${parameters.sessionId}) does not exist")
                        is CloseSessionResult.SessionWasNotClosed ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) was not closed")
                        is CloseSessionResult.Success ->
                            call.respond(HttpStatusCode.OK)
                    }
                }

            }

        }

    }

}
