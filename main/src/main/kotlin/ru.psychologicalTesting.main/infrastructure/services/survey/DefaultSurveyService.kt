package ru.psychologicalTesting.main.infrastructure.services.survey

import kotlinx.datetime.LocalDateTime
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.survey.session.FullSurveySession
import ru.psychologicalTesting.common.survey.session.NewSurveySession
import ru.psychologicalTesting.common.survey.session.SurveySession
import ru.psychologicalTesting.common.testing.session.SessionAnswer
import ru.psychologicalTesting.main.infrastructure.repositories.survey.session.SurveySessionRepository
import ru.psychologicalTesting.main.infrastructure.repositories.survey.survey.SurveyRepository
import ru.psychologicalTesting.main.infrastructure.repositories.testing.question.QuestionRepository
import ru.psychologicalTesting.main.infrastructure.services.survey.results.CloseSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.CompleteSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.CreateSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.GetSurveySessionResult
import ru.psychologicalTesting.main.infrastructure.services.survey.results.UpdateSurveyAnswersResult
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class DefaultSurveyService(
    private val sessionRepository: SurveySessionRepository,
    private val surveyRepository: SurveyRepository,
    private val questionRepository: QuestionRepository
) : SurveyService {

    override fun createSession(
        userId: UUID,
        surveyId: UUID
    ): CreateSurveySessionResult {

        val existing = sessionRepository.findOneByUserIdWithSurveyId(
            userId = userId,
            surveyId = surveyId
        )

        if (existing != null && existing.status == SurveySession.Status.IN_PROGRESS) {
            return CreateSurveySessionResult.SurveyAlreadyStarted
        }

        val survey = surveyRepository.findOneById(surveyId)
            ?: return CreateSurveySessionResult.SurveyNotFound

        if (!survey.isActive) {
            return CreateSurveySessionResult.SurveyNotActive
        }

        val questions = questionRepository.findAllBySurveyId(surveyId)

        val newSession = sessionRepository.create(
            dto = NewSurveySession(
                userId = userId,
                surveyId = surveyId
            )
        )

        return CreateSurveySessionResult.Success(
            createdSession = FullSurveySession(
                id = newSession.id,
                userId = newSession.userId,
                surveyId = newSession.surveyId,
                questions = questions,
                answers = newSession.answers,
                status = newSession.status,
                createdAt = newSession.createdAt,
                closedAt = newSession.closedAt
            )
        )
    }

    override fun getSessionById(
        sessionId: UUID
    ): GetSurveySessionResult {

        val session = sessionRepository.findOneById(sessionId)
            ?: return GetSurveySessionResult.Error

        val questions = questionRepository.findAllBySurveyId(session.surveyId)

        return GetSurveySessionResult.Success(
            session = FullSurveySession(
                id = session.id,
                userId = session.userId,
                surveyId = session.surveyId,
                questions = questions,
                answers = session.answers,
                status = session.status,
                createdAt = session.createdAt,
                closedAt = session.closedAt
            )
        )
    }

    override fun updateAnswers(
        sessionId: UUID,
        answers: List<SessionAnswer>
    ): UpdateSurveyAnswersResult {

        val session = sessionRepository.findOneById(sessionId)
            ?: return UpdateSurveyAnswersResult.SessionNotFound

        if (session.status != SurveySession.Status.IN_PROGRESS) {
            return UpdateSurveyAnswersResult.SessionClosed
        }

        val updatedSession = session.copy(answers = answers)

        val isUpdated = sessionRepository.update(
            id = sessionId,
            dto = updatedSession
        )

        if (!isUpdated) {
            return UpdateSurveyAnswersResult.AnswerWasNotUpdated
        }

        return UpdateSurveyAnswersResult.Success
    }

    override fun completeSession(
        sessionId: UUID
    ): CompleteSurveySessionResult {

        val session = sessionRepository.findOneById(sessionId)
            ?: return CompleteSurveySessionResult.SessionNotFound

        if (session.status != SurveySession.Status.IN_PROGRESS) {
            return CompleteSurveySessionResult.SessionMustBeOpened
        }

        surveyRepository.findOneById(session.surveyId)
            ?: return CompleteSurveySessionResult.SurveyNotFound

        val questions = questionRepository.findAllBySurveyId(session.surveyId)

        val answeredIds = session.answers
            .filter {
                it.selectedIndex != null ||
                    !it.selectedIndices.isNullOrEmpty() ||
                    !it.textAnswer.isNullOrBlank()
            }
            .map { it.questionId }
            .toSet()

        if (questions.any { it.id !in answeredIds }) {
            return CompleteSurveySessionResult.SurveyIsNotCompleted
        }

        val updatedSession = session.copy(
            status = SurveySession.Status.COMPLETED,
            closedAt = LocalDateTime.now()
        )

        val isUpdated = sessionRepository.update(
            id = sessionId,
            dto = updatedSession
        )

        if (!isUpdated) {
            return CompleteSurveySessionResult.SessionUpdateError
        }

        return CompleteSurveySessionResult.Success(
            session = FullSurveySession(
                id = updatedSession.id,
                userId = updatedSession.userId,
                surveyId = updatedSession.surveyId,
                questions = questions,
                answers = updatedSession.answers,
                status = updatedSession.status,
                createdAt = updatedSession.createdAt,
                closedAt = updatedSession.closedAt
            )
        )
    }

    override fun closeSession(
        sessionId: UUID
    ): CloseSurveySessionResult {

        val session = sessionRepository.findOneById(sessionId)
            ?: return CloseSurveySessionResult.SessionNotFound

        if (session.status != SurveySession.Status.IN_PROGRESS) {
            return CloseSurveySessionResult.SessionMustBeOpened
        }

        val updatedSession = session.copy(
            status = SurveySession.Status.CLOSED,
            closedAt = LocalDateTime.now()
        )

        val isUpdated = sessionRepository.update(
            id = sessionId,
            dto = updatedSession
        )

        if (!isUpdated) {
            return CloseSurveySessionResult.SessionWasNotClosed
        }

        return CloseSurveySessionResult.Success
    }

}
