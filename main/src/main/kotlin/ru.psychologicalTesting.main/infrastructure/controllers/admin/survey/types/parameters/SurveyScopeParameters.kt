package ru.psychologicalTesting.main.infrastructure.controllers.admin.survey.types.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

open class SurveyScopeParameters : RouteParameters() {
    val surveyId by path.uuid {
        name = "survey_id"
        description = "Survey id to manipulate"
    }
}
