package ru.psychologicalTesting.common.testing.question

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

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
