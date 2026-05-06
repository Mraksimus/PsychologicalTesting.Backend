package ru.psychologicalTesting.main.infrastructure.services.testing

import kotlinx.datetime.LocalDateTime
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.testing.question.QuestionContentType
import ru.psychologicalTesting.common.testing.session.FullTestingSession
import ru.psychologicalTesting.common.testing.session.NewTestingSession
import ru.psychologicalTesting.common.testing.session.SessionAnswer
import ru.psychologicalTesting.common.testing.session.TestingSession
import ru.psychologicalTesting.main.infrastructure.repositories.testing.question.QuestionRepository
import ru.psychologicalTesting.main.infrastructure.repositories.testing.session.TestingSessionRepository
import ru.psychologicalTesting.main.infrastructure.repositories.testing.test.TestRepository
import ru.psychologicalTesting.main.infrastructure.services.llm.LLMService
import ru.psychologicalTesting.main.infrastructure.services.llm.results.PromptResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CloseSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CompleteSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CreateSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.GetSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.UpdateAnswersResult
import ru.psychologicalTesting.main.plugins.suspendedTransaction
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class DefaultTestingService(
    private val llmService: LLMService,
    private val sessionRepository: TestingSessionRepository,
    private val testRepository: TestRepository,
    private val questionRepository: QuestionRepository
) : TestingService {

    override fun createSession(
        userId: UUID,
        testId: UUID
    ): CreateSessionResult {

        val session = sessionRepository.findOneByUserIdWithTestId(
            userId = userId,
            testId = testId
        )

        if (session != null && session.status == TestingSession.Status.IN_PROGRESS) {
            return CreateSessionResult.TestAlreadyStarted
        }

        testRepository.findOneById(testId) ?: return CreateSessionResult.TestNotFound

        val questions = questionRepository.findAllByTestId(testId)

        val newSession = sessionRepository.create(
            dto = NewTestingSession(
                userId = userId,
                testId = testId
            )
        )

        return CreateSessionResult.Success(
            createdSession = FullTestingSession(
                id = newSession.id,
                userId = newSession.userId,
                testId = newSession.testId,
                questions = questions,
                answers = newSession.answers,
                result = newSession.result,
                status = newSession.status,
                createdAt = newSession.createdAt,
                closedAt = newSession.closedAt
            )
        )
    }

    override fun getSessionById(
        sessionId: UUID
    ): GetSessionResult {

        val session = sessionRepository.findOneById(sessionId) ?: return GetSessionResult.Error
        val questions = questionRepository.findAllByTestId(session.testId)

        return GetSessionResult.Success(
            session = FullTestingSession(
                id = session.id,
                userId = session.userId,
                testId = session.testId,
                questions = questions,
                answers = session.answers,
                result = session.result,
                status = session.status,
                createdAt = session.createdAt,
                closedAt = session.closedAt
            )
        )
    }

    override fun updateAnswers(
        sessionId: UUID,
        answers: List<SessionAnswer>
    ): UpdateAnswersResult {

        val session = sessionRepository.findOneById(sessionId) ?: return UpdateAnswersResult.SessionNotFound

        if (session.status != TestingSession.Status.IN_PROGRESS) {
            return UpdateAnswersResult.SessionClosed
        }

        val updatedSession = session.copy(
            answers = answers
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

    override suspend fun completeSession(
        sessionId: UUID
    ): CompleteSessionResult {

        val session = sessionRepository.findOneById(sessionId) ?: return CompleteSessionResult.SessionNotFound

        if (session.status != TestingSession.Status.IN_PROGRESS) {
            return CompleteSessionResult.SessionMustBeOpened
        }

        val test = testRepository.findOneById(session.testId)!!

        val questions = questionRepository.findAllByTestId(session.testId)

        val answeredIds = session.answers.filter {
            it.selectedIndex != null
        }.map {
            it.questionId
        }.toSet()

        if (questions.any { it.id !in answeredIds }) {
            return CompleteSessionResult.TestIsNotCompleted
        }

        val totalScore = questions.sumOf { question ->
            val answer = session.answers.first { it.questionId == question.id }
            val content = question.content

            if (content is QuestionContentType.Choice && answer.selectedIndex != null) {
                content.options[answer.selectedIndex!!].score
            } else {
                0
            }
        }

        val requestResult = llmService.sendTestResult(
            test = test,
            questions = questions,
            answers = session.answers,
            totalScore = totalScore,
        )

        val llmResponse = if (requestResult is PromptResult.Success) {
            requestResult.llmResponse
        } else {
            return CompleteSessionResult.LLMRequestError
        }

        val updatedSession = session.copy(
            result = llmResponse.message,
            status = TestingSession.Status.COMPLETED,
            closedAt = LocalDateTime.now()
        )

        val isUpdated = suspendedTransaction {
            sessionRepository.update(
                id = sessionId,
                dto = updatedSession
            )
        }

        if (!isUpdated) {
            return CompleteSessionResult.SessionUpdateError
        }

        return CompleteSessionResult.Success(
            session = FullTestingSession(
                id = updatedSession.id,
                userId = updatedSession.userId,
                testId = updatedSession.testId,
                questions = questions,
                answers = updatedSession.answers,
                result = updatedSession.result,
                status = updatedSession.status,
                createdAt = updatedSession.createdAt,
                closedAt = updatedSession.closedAt,
            )
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
