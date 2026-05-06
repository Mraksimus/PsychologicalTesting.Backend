package ru.psychologicalTesting.main.infrastructure.services.testing

import ru.psychologicalTesting.common.testing.session.SessionAnswer
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CloseSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CompleteSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CreateSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.GetSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.UpdateAnswersResult
import java.util.*

interface TestingService {

    fun createSession(
        userId: UUID,
        testId: UUID
    ): CreateSessionResult

    fun getSessionById(
        sessionId: UUID
    ): GetSessionResult

    fun updateAnswers(
        sessionId: UUID,
        answers: List<SessionAnswer>
    ): UpdateAnswersResult

    suspend fun completeSession(
        sessionId: UUID
    ): CompleteSessionResult

    fun closeSession(
        sessionId: UUID
    ): CloseSessionResult

}
