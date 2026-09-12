package ru.psychologicalTesting.main.infrastructure.controllers.survey.session.types.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

class SurveySessionScopeParameters : RouteParameters() {
    val sessionId by path.uuid {
        name = "session_id"
        description = "Survey session id to manipulate"
    }
}
