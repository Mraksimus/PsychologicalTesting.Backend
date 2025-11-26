package ru.psychologicalTesting.common.types.testTranscription

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.question.ExistingQuestion
import ru.psychologicalTesting.common.testing.session.ExistingTestingSession
import ru.psychologicalTesting.common.testing.test.ExistingTest

@Serializable
data class LLMTestTranscriptionRequest(
    val test: ExistingTest,
    val questions: List<ExistingQuestion>,
    val session: ExistingTestingSession
)
