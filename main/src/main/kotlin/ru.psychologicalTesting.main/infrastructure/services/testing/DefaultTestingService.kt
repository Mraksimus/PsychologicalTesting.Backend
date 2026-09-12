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
import ru.psychologicalTesting.main.infrastructure.services.testing.results.RegenerateResultResult
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

        val test = testRepository.findOneById(testId) ?: return CreateSessionResult.TestNotFound

        if (!test.isActive) {
            return CreateSessionResult.TestNotActive
        }

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
            it.selectedIndex != null ||
                !it.selectedIndices.isNullOrEmpty() ||
                !it.textAnswer.isNullOrBlank()
        }.map {
            it.questionId
        }.toSet()

        if (questions.any { it.id !in answeredIds }) {
            return CompleteSessionResult.TestIsNotCompleted
        }

        val totalScore = questions.sumOf { question ->
            val answer = session.answers.first { it.questionId == question.id }
            val content = question.content

            if (content !is QuestionContentType.Choice) {
                return@sumOf 0
            }

            val indices = when {
                answer.selectedIndex != null -> listOf(answer.selectedIndex!!)
                !answer.selectedIndices.isNullOrEmpty() -> answer.selectedIndices!!
                else -> emptyList()
            }

            indices.sumOf { idx ->
                content.options.getOrNull(idx)?.score ?: 0
            }
        }

        val requestResult = llmService.sendTestResult(
            test = test,
            questions = questions,
            answers = session.answers,
            totalScore = totalScore,
        )

        val llmMessage = (requestResult as? PromptResult.Success)?.llmResponse?.message

        val updatedSession = session.copy(
            result = llmMessage,
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

    override suspend fun regenerateResult(
        sessionId: UUID
    ): RegenerateResultResult {

        val session = sessionRepository.findOneById(sessionId)
            ?: return RegenerateResultResult.SessionNotFound

        if (session.status != TestingSession.Status.COMPLETED) {
            return RegenerateResultResult.SessionNotCompleted
        }

        val test = testRepository.findOneById(session.testId)
            ?: return RegenerateResultResult.TestNotFound

        val questions = questionRepository.findAllByTestId(session.testId)

        val totalScore = questions.sumOf { question ->
            val answer = session.answers.firstOrNull { it.questionId == question.id }
                ?: return@sumOf 0
            val content = question.content

            if (content !is QuestionContentType.Choice) {
                return@sumOf 0
            }

            val indices = when {
                answer.selectedIndex != null -> listOf(answer.selectedIndex!!)
                !answer.selectedIndices.isNullOrEmpty() -> answer.selectedIndices!!
                else -> emptyList()
            }

            indices.sumOf { idx ->
                content.options.getOrNull(idx)?.score ?: 0
            }
        }

        val requestResult = llmService.sendTestResult(
            test = test,
            questions = questions,
            answers = session.answers,
            totalScore = totalScore,
        )

        val llmMessage = (requestResult as? PromptResult.Success)?.llmResponse?.message
            ?: return RegenerateResultResult.LLMRequestError

        val updatedSession = session.copy(result = llmMessage)

        val isUpdated = suspendedTransaction {
            sessionRepository.update(id = sessionId, dto = updatedSession)
        }

        if (!isUpdated) {
            return RegenerateResultResult.SessionUpdateError
        }

        return RegenerateResultResult.Success(
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
