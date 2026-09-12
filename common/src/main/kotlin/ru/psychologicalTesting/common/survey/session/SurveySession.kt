package ru.psychologicalTesting.common.survey.session

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID
import ru.psychologicalTesting.common.testing.question.Question
import ru.psychologicalTesting.common.testing.session.SessionAnswer

@Serializable
data class NewSurveySession(
    override val userId: SerialUUID,
    override val surveyId: SerialUUID
) : SurveySession

@Serializable
data class ExistingSurveySession(
    val id: SerialUUID,
    override val userId: SerialUUID,
    override val surveyId: SerialUUID,
    val answers: List<SessionAnswer>,
    val status: SurveySession.Status,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime? = null
) : SurveySession

@Serializable
data class FullSurveySession(
    val id: SerialUUID,
    override val userId: SerialUUID,
    override val surveyId: SerialUUID,
    val questions: List<Question>,
    val answers: List<SessionAnswer>,
    val status: SurveySession.Status,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime? = null
) : SurveySession

sealed interface SurveySession {

    val userId: SerialUUID
    val surveyId: SerialUUID

    enum class Status {
        IN_PROGRESS,
        COMPLETED,
        CLOSED
    }

}
