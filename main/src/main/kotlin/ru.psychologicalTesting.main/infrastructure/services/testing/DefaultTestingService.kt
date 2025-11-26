package ru.psychologicalTesting.main.infrastructure.services.testing

import kotlinx.datetime.LocalDateTime
import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.infrastructure.dto.testing.question.ExistingQuestion
import ru.psychologicalTesting.main.infrastructure.dto.testing.question.toClient
import ru.psychologicalTesting.main.infrastructure.dto.testing.session.NewTestingSession
import ru.psychologicalTesting.main.infrastructure.dto.testing.session.TestingSession
import ru.psychologicalTesting.main.infrastructure.repositories.testing.question.QuestionRepository
import ru.psychologicalTesting.main.infrastructure.repositories.testing.session.TestingSessionRepository
import ru.psychologicalTesting.main.infrastructure.repositories.testing.test.TestRepository
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CloseSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CompleteSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CreateSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.UpdateAnswersResult
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class DefaultTestingService(
    private val sessionRepository: TestingSessionRepository,
    private val testRepository: TestRepository,
    private val questionRepository: QuestionRepository
) : TestingService {

    override fun createSession(
        userId: UUID,
        testId: UUID
    ): CreateSessionResult {

        val session = sessionRepository.findOneByTestId(testId)
        if (session != null && session.status == TestingSession.Status.IN_PROGRESS) {
            return CreateSessionResult.TestAlreadyStarted
        }

        testRepository.findOneById(testId) ?: return CreateSessionResult.TestNotFound

        val questions = questionRepository.findAllByTestId(testId)

        val questionResponses = questions.map { question ->
            question.copy(
                content = question.content.toClient(),
            )
        }

        val newSession = sessionRepository.create(
            dto = NewTestingSession(
                userId = userId,
                testId = testId,
                questionResponses = questionResponses
            )
        )

        return CreateSessionResult.Success(
            createdSession = newSession
        )
    }

    override fun updateAnswers(
        sessionId: UUID,
        questionResponses: List<ExistingQuestion>
    ): UpdateAnswersResult {

        val session = sessionRepository.findOneById(sessionId) ?: return UpdateAnswersResult.SessionNotFound

        if (session.status != TestingSession.Status.IN_PROGRESS) {
            return UpdateAnswersResult.SessionClosed
        }

        val questionResponses = questionResponses.map { question ->
            question.copy(
                content = question.content.toClient(),
            )
        }

        val updatedSession = session.copy(
            questionResponses = questionResponses,
        )

        val isUpdate = sessionRepository.update(
            id = sessionId,
            dto = updatedSession
        )

        if (!isUpdate) {
            return UpdateAnswersResult.AnswerWasNotUpdated
        }

        return UpdateAnswersResult.Success
    }

    override fun completeSession(
        sessionId: UUID
    ): CompleteSessionResult {

        val session = sessionRepository.findOneById(sessionId) ?: return CompleteSessionResult.SessionNotFound

        if (session.status != TestingSession.Status.IN_PROGRESS) {
            return CompleteSessionResult.SessionMustBeOpened
        }

        val testQuestions = questionRepository.findAllByTestId(session.testId)

        if (testQuestions.size != session.questionResponses.size) {
            return CompleteSessionResult.TestIsNotCompleted
        }

        // TODO: Запрос к LLM для расшифровки теста
        // TODO: Переводить ответы обратно в FullAnswer для отправки к LLM

        val updatedSession = session.copy(
            result = "Test completed",
            status = TestingSession.Status.COMPLETED,
            closedAt = LocalDateTime.now()
        )

        val isUpdated = sessionRepository.update(
            id = sessionId,
            dto = updatedSession
        )

        if (!isUpdated) {
            return CompleteSessionResult.TestIsNotCompleted
        }

        return CompleteSessionResult.Success(
            session = updatedSession
        )
    }

    override fun closeSession(
        sessionId: UUID,
    ): CloseSessionResult {

        val session = sessionRepository.findOneById(sessionId) ?: return CloseSessionResult.SessionNotFound

        if (session.status != TestingSession.Status.IN_PROGRESS) {
            return CloseSessionResult.SessionMustBeOpened
        }

        val updatedSession = session.copy(
            status = TestingSession.Status.CLOSED,
            closedAt = LocalDateTime.now()
        )

        val isUpdate = sessionRepository.update(
            id = sessionId,
            dto = updatedSession
        )

        if (!isUpdate) {
            return CloseSessionResult.SessionWasNotClosed
        }

        return CloseSessionResult.Success
    }

}
