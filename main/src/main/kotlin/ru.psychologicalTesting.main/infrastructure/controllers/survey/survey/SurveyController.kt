package ru.psychologicalTesting.main.infrastructure.controllers.survey.survey

import dev.h4kt.ktorDocs.dsl.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.common.survey.survey.ExistingSurvey
import ru.psychologicalTesting.main.infrastructure.controllers.common.parameters.PageParameters
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.repositories.survey.survey.SurveyRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

fun Routing.configureSurveyRouting() = route("/surveys") {
    configurePublicRoutes()
}

private fun Route.configurePublicRoutes() {

    val surveyRepository by inject<SurveyRepository>()

    get(::PageParameters) {

        description = "Get all active surveys"
        tags = listOf("Surveys")

        responses {
            HttpStatusCode.OK returns typeInfo<PageResponse<ExistingSurvey>>()
        }

        handle {

            val result = suspendedTransaction {
                surveyRepository.findAllPage(
                    offset = parameters.offset,
                    limit = parameters.limit,
                    activeOnly = true
                )
            }

            call.respond(result)
        }
    }
}
