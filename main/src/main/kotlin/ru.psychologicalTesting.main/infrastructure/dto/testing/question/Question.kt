package ru.psychologicalTesting.main.infrastructure.dto.testing.question

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.compat.SerialUUID

@Serializable
data class NewQuestion(
    override val testId: SerialUUID,
    override val content: QuestionContentType
) : Question

@Serializable
data class ExistingQuestion(
    val id: SerialUUID,
    override val testId: SerialUUID,
    override val content: QuestionContentType,
    val position: Int
) : Question

sealed interface Question {
    val testId: SerialUUID
    val content: QuestionContentType
}
