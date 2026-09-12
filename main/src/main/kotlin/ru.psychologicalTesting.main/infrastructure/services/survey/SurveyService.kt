package ru.psychologicalTesting.main.infrastructure.services.survey

import ru.psychologicalTesting.common.testing.session.SessionAnswer
import ru.psychologicalTesting.main.infrastructure.services.survey.results.CloseSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.CompleteSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.CreateSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.GetSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.UpdateSurveyAnswersResult
import java.util.*

interface SurveyService {

    fun createSession(
        userId: UUID,
        surveyId: UUID
    ): CreateSurveySessionResult

    fun getSessionById(
        sessionId: UUID
    ): GetSurveySessionResult

    fun updateAnswers(
        sessionId: UUID,
        answers: List<SessionAnswer>
    ): UpdateSurveyAnswersResult

    fun completeSession(
        sessionId: UUID
    ): CompleteSurveySessionResult

    fun closeSession(
        sessionId: UUID
    ): CloseSurveySessionResult

}
