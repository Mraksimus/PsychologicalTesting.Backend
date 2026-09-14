package ru.psychologicalTesting.main.infrastructure.controllers.survey.session.types.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

class CreateSurveySessionParameters : RouteParameters() {
    val surveyId by path.uuid {
        name = "survey_id"
        description = "Survey id to start a session for"
    }
}
