package ru.psychologicalTesting.main.infrastructure.controllers.admin.survey

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
import ru.psychologicalTesting.common.survey.survey.ExistingSurvey
import ru.psychologicalTesting.common.survey.survey.NewSurvey
import ru.psychologicalTesting.main.infrastructure.controllers.admin.survey.types.parameters.SurveyPageParameters
import ru.psychologicalTesting.main.infrastructure.controllers.admin.survey.types.parameters.SurveyScopeParameters
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.requirePermissions
import ru.psychologicalTesting.main.infrastructure.controllers.common.requests.ReorderRequest
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ForbiddenResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.dto.survey.SurveyDetails
import ru.psychologicalTesting.main.infrastructure.repositories.survey.survey.SurveyRepository
import ru.psychologicalTesting.main.infrastructure.repositories.testing.question.QuestionRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Surveys (admin)"

fun Routing.configureAdminSurveyRouting() = route("/admin/surveys") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    val surveyRepository by inject<SurveyRepository>()
    val questionRepository by inject<QuestionRepository>()

    requirePermissions(Permission.SURVEYS_VIEW) {

        get(::SurveyPageParameters) {

            description = "List all surveys (paged)"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<PageResponse<ExistingSurvey>>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {
                val result = suspendedTransaction {
                    surveyRepository.findAllPage(
                        offset = parameters.offset,
                        limit = parameters.limit
                    )
                }
                call.respond(result)
            }
        }

        get("{survey_id}", ::SurveyScopeParameters) {

            description = "Get a survey with its questions"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<SurveyDetails>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Survey does not exist"
                }
            }

            handle {

                val details = suspendedTransaction {
                    val survey = surveyRepository.findOneById(parameters.surveyId)
                        ?: return@suspendedTransaction null

                    val questions = questionRepository
                        .findAllBySurveyId(parameters.surveyId)
                        .sortedBy { it.position }

                    SurveyDetails(survey, questions)
                }

                if (details == null) {
                    call.respondNotFound("Survey(id=${parameters.surveyId}) does not exist")
                } else {
                    call.respond(details)
                }
            }
        }
    }

    requirePermissions(Permission.SURVEYS_EDIT) {

        post("new") {

            description = "Create a new survey"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewSurvey>()

            responses {
                HttpStatusCode.Created returns typeInfo<ExistingSurvey>()
                HttpStatusCode.BadRequest returns typeInfo<ProblemDetailsMessage>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {

                val body = call.receive<NewSurvey>()

                val survey = suspendedTransaction {
                    surveyRepository.create(body)
                }

                call.respond(HttpStatusCode.Created, survey)
            }
        }

        patch("{survey_id}", ::SurveyScopeParameters) {

            description = "Update a survey"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewSurvey>()

            responses {
                noContent()
                HttpStatusCode.BadRequest returns typeInfo<ProblemDetailsMessage>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Survey does not exist"
                }
            }

            handle {

                val body = call.receive<NewSurvey>()

                val isUpdated = suspendedTransaction {
                    surveyRepository.update(
                        id = parameters.surveyId,
                        dto = body
                    )
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Survey(id=${parameters.surveyId}) does not exist")
                }
            }
        }

        put("{survey_id}/position", ::SurveyScopeParameters) {

            description = "Reorder a survey"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<ReorderRequest>()

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Survey does not exist"
                }
            }

            handle {

                val body = call.receive<ReorderRequest>()

                val isUpdated = suspendedTransaction {
                    surveyRepository.updatePositionById(
                        id = parameters.surveyId,
                        position = body.position
                    )
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Survey(id=${parameters.surveyId}) does not exist")
                }
            }
        }

        delete("{survey_id}", ::SurveyScopeParameters) {

            description = "Delete a survey"
            tags = listOf(SWAGGER_TAG)

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Survey does not exist"
                }
            }

            handle {

                val isDeleted = suspendedTransaction {
                    surveyRepository.delete(parameters.surveyId)
                }

                if (isDeleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Survey(id=${parameters.surveyId}) does not exist")
                }
            }
        }
    }
}
