package ru.psychologicalTesting.main.infrastructure.services.testing

import ru.psychologicalTesting.main.infrastructure.dto.testing.question.ExistingQuestion
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CloseSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CompleteSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.CreateSessionResult
import ru.psychologicalTesting.main.infrastructure.services.testing.results.UpdateAnswersResult
import java.util.*

interface TestingService {

    fun createSession(
        userId: UUID,
        testId: UUID
    ): CreateSessionResult

    fun updateAnswers(
        sessionId: UUID,
        questionResponses: List<ExistingQuestion>
    ): UpdateAnswersResult

    fun completeSession(
        sessionId: UUID
    ): CompleteSessionResult

    fun closeSession(
        sessionId: UUID
    ): CloseSessionResult

}
