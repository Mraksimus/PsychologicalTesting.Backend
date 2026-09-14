package ru.psychologicalTesting.main.infrastructure.controllers.survey.session.requests

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.session.SessionAnswer

@Serializable
data class UpdateSurveyAnswersRequest(
    val answers: List<SessionAnswer>
)
