package ru.psychologicalTesting.common.testing.session

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID
import ru.psychologicalTesting.common.testing.question.Question

@Serializable
data class NewTestingSession(
    override val userId: SerialUUID,
    override val testId: SerialUUID
) : TestingSession

@Serializable
data class ExistingTestingSession(
    val id: SerialUUID,
    override val userId: SerialUUID,
    override val testId: SerialUUID,
    val answers: List<SessionAnswer>,
    val result: String? = null,
    val status: TestingSession.Status,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime? = null
) : TestingSession

@Serializable
data class FullTestingSession(
    val id: SerialUUID,
    override val userId: SerialUUID,
    override val testId: SerialUUID,
    val questions: List<Question>,
    val answers: List<SessionAnswer>,
    val result: String? = null,
    val status: TestingSession.Status,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime? = null
) : TestingSession

sealed interface TestingSession {

    val userId: SerialUUID
    val testId: SerialUUID

    enum class Status {
        IN_PROGRESS,
        COMPLETED,
        CLOSED
    }

}
