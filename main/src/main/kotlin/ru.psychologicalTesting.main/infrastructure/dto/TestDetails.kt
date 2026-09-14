package ru.psychologicalTesting.main.infrastructure.dto

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.question.ExistingQuestion
import ru.psychologicalTesting.common.testing.test.ExistingTest

@Serializable
data class TestDetails(
    val test: ExistingTest,
    val questions: List<ExistingQuestion>
)
