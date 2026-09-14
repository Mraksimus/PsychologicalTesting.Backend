package ru.psychologicalTesting.main.infrastructure.dto.survey

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID
import ru.psychologicalTesting.common.survey.session.SurveySession
import ru.psychologicalTesting.common.testing.session.SessionAnswer

@Serializable
data class AdminSurveySessionItem(
    val id: SerialUUID,
    val userId: SerialUUID,
    val userFullName: String,
    val userEmail: String,
    val surveyId: SerialUUID,
    val status: SurveySession.Status,
    val answers: List<SessionAnswer>,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime? = null
)
