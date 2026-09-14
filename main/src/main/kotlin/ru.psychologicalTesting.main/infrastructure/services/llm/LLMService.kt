package ru.psychologicalTesting.main.infrastructure.services.llm

import ru.psychologicalTesting.common.testing.question.ExistingQuestion
import ru.psychologicalTesting.common.testing.session.SessionAnswer
import ru.psychologicalTesting.common.testing.test.ExistingTest
import ru.psychologicalTesting.main.infrastructure.services.llm.results.PromptResult
import java.util.*

interface LLMService {

    suspend fun sendPrompt(
        userId: UUID,
        prompt: String
    ): PromptResult

    suspend fun sendTestResult(
        test: ExistingTest,
        questions: List<ExistingQuestion>,
        answers: List<SessionAnswer>,
        totalScore: Int
    ): PromptResult

}
