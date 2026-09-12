package ru.psychologicalTesting.main.infrastructure.controllers.admin.survey.session

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
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.dto.survey.AdminSurveySessionItem
import ru.psychologicalTesting.main.infrastructure.repositories.survey.session.SurveySessionRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Survey sessions (admin)"

class AdminSurveySessionPageParameters : RouteParameters() {

    val surveyId by path.uuid {
        name = "survey_id"
        description = "Survey id"
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

fun Routing.configureAdminSurveySessionRouting() = route("/admin/surveys/{survey_id}/sessions") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    val sessionRepository by inject<SurveySessionRepository>()

    requirePermissions(Permission.SURVEY_SESSIONS_VIEW) {

        get(::AdminSurveySessionPageParameters) {

            description = "List sessions of a survey (paged) with respondent info"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<PageResponse<AdminSurveySessionItem>>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {

                val result = suspendedTransaction {
                    sessionRepository.findAllAdminBySurveyIdPaged(
                        surveyId = parameters.surveyId,
                        offset = parameters.offset,
                        limit = parameters.limit
                    )
                }

                call.respond(result)
            }
        }
    }
}
