package ru.psychologicalTesting.main.infrastructure.controllers.survey.session

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
import ru.psychologicalTesting.common.survey.session.ExistingSurveySession
import ru.psychologicalTesting.common.survey.session.FullSurveySession
import ru.psychologicalTesting.main.infrastructure.controllers.common.parameters.PageParameters
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.BadRequestResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ConflictResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondBadRequest
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondConflict
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.controllers.common.utils.transparentRoute
import ru.psychologicalTesting.main.infrastructure.controllers.survey.session.middleware.SurveySessionOwnershipPlugin
import ru.psychologicalTesting.main.infrastructure.controllers.survey.session.requests.UpdateSurveyAnswersRequest
import ru.psychologicalTesting.main.infrastructure.controllers.survey.session.types.parameters.CreateSurveySessionParameters
import ru.psychologicalTesting.main.infrastructure.controllers.survey.session.types.parameters.SurveySessionScopeParameters
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.repositories.survey.session.SurveySessionRepository
import ru.psychologicalTesting.main.infrastructure.services.survey.SurveyService
import ru.psychologicalTesting.main.infrastructure.services.survey.results.CloseSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.CompleteSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.CreateSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.GetSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.UpdateSurveyAnswersResult
import ru.psychologicalTesting.main.plugins.authentication.UserPrincipal
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Survey sessions"

fun Routing.configureSurveySessionRouting() = route("/surveys/sessions") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    val sessionRepository by inject<SurveySessionRepository>()
    val surveyService by inject<SurveyService>()

    post("/{survey_id}", ::CreateSurveySessionParameters) {

        description = "Create a new survey session"
        tags = listOf(SWAGGER_TAG)

        responses {
            HttpStatusCode.OK returns typeInfo<FullSurveySession>()
            HttpStatusCode.Conflict returns typeInfo<ConflictResponse>()
            HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
            HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
        }

        handle {

            val (userId) = call.principal<UserPrincipal>()!!

            val result = suspendedTransaction {
                surveyService.createSession(
                    userId = userId,
                    surveyId = parameters.surveyId
                )
            }

            when (result) {
                is CreateSurveySessionResult.SurveyNotFound ->
                    call.respondNotFound("Survey (id=${parameters.surveyId}) does not exist")
                is CreateSurveySessionResult.SurveyNotActive ->
                    call.respondBadRequest("Survey (id=${parameters.surveyId}) is not active")
                is CreateSurveySessionResult.SurveyAlreadyStarted ->
                    call.respondConflict("Survey session with survey(id=${parameters.surveyId}) already started")
                is CreateSurveySessionResult.Success ->
                    call.respond(result.createdSession)
            }
        }
    }

    get(::PageParameters) {

        description = "Get all user survey sessions"
        tags = listOf(SWAGGER_TAG)

        responses {
            HttpStatusCode.OK returns typeInfo<PageResponse<ExistingSurveySession>>()
            HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
        }

        handle {

            val (userId) = call.principal<UserPrincipal>()!!

            val result = suspendedTransaction {
                sessionRepository.findAllByUserIdPaged(
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

            install(SurveySessionOwnershipPlugin)

            get(::SurveySessionScopeParameters) {

                description = "Get user survey session"
                tags = listOf(SWAGGER_TAG)

                responses {
                    HttpStatusCode.OK returns typeInfo<FullSurveySession>()
                    HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
                }

                handle {

                    val result = suspendedTransaction {
                        surveyService.getSessionById(parameters.sessionId)
                    }

                    if (result is GetSurveySessionResult.Success) {
                        call.respond(result.session)
                    } else {
                        call.respondNotFound("Session(id=${parameters.sessionId}) does not exist")
                    }
                }
            }

            put("/answers", ::SurveySessionScopeParameters) {

                description = "Update survey session answers"
                tags = listOf(SWAGGER_TAG)

                requestBody = typeInfo<UpdateSurveyAnswersRequest>()

                responses {
                    HttpStatusCode.OK returns nothing
                    HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
                    HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
                }

                handle {

                    val (answers) = call.receive<UpdateSurveyAnswersRequest>()

                    val result = suspendedTransaction {
                        surveyService.updateAnswers(
                            sessionId = parameters.sessionId,
                            answers = answers
                        )
                    }

                    when (result) {
                        is UpdateSurveyAnswersResult.SessionNotFound ->
                            call.respondNotFound("Session(id=${parameters.sessionId}) does not exist")
                        is UpdateSurveyAnswersResult.SessionClosed ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) closed")
                        is UpdateSurveyAnswersResult.AnswerWasNotUpdated ->
                            call.respond(
                                status = HttpStatusCode.InternalServerError,
                                message = "Session(id=${parameters.sessionId}) not updated"
                            )
                        is UpdateSurveyAnswersResult.Success ->
                            call.respond(HttpStatusCode.OK)
                    }
                }
            }

            put("/complete", ::SurveySessionScopeParameters) {

                description = "Complete survey session"
                tags = listOf(SWAGGER_TAG)

                responses {
                    HttpStatusCode.OK returns typeInfo<CompleteSurveySessionResult.Success>()
                    HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
                    HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
                }

                handle {

                    val result = suspendedTransaction {
                        surveyService.completeSession(parameters.sessionId)
                    }

                    when (result) {
                        is CompleteSurveySessionResult.SessionNotFound ->
                            call.respondNotFound("Session(id=${parameters.sessionId}) does not exist")
                        is CompleteSurveySessionResult.SurveyNotFound ->
                            call.respondNotFound("Survey not found")
                        is CompleteSurveySessionResult.SessionUpdateError ->
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                "Session(id=${parameters.sessionId}) update failed"
                            )
                        is CompleteSurveySessionResult.SessionMustBeOpened ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) must be opened")
                        is CompleteSurveySessionResult.SurveyIsNotCompleted ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) is not completed")
                        is CompleteSurveySessionResult.Success ->
                            call.respond(HttpStatusCode.OK, result)
                    }
                }
            }

            put("/close", ::SurveySessionScopeParameters) {

                description = "Close survey session"
                tags = listOf(SWAGGER_TAG)

                responses {
                    HttpStatusCode.OK returns nothing
                    HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
                    HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
                }

                handle {

                    val result = suspendedTransaction {
                        surveyService.closeSession(parameters.sessionId)
                    }

                    when (result) {
                        is CloseSurveySessionResult.SessionMustBeOpened ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) must be opened")
                        is CloseSurveySessionResult.SessionNotFound ->
                            call.respondNotFound("Session(id=${parameters.sessionId}) does not exist")
                        is CloseSurveySessionResult.SessionWasNotClosed ->
                            call.respondBadRequest("Session(id=${parameters.sessionId}) was not closed")
                        is CloseSurveySessionResult.Success ->
                            call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}
