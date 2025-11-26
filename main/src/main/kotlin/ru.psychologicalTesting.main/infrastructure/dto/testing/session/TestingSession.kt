package ru.psychologicalTesting.main.infrastructure.dto.testing.session

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.compat.SerialUUID
import ru.psychologicalTesting.main.infrastructure.dto.testing.question.ExistingQuestion

@Serializable
data class NewTestingSession(
    override val userId: SerialUUID,
    override val testId: SerialUUID,
    override val questionResponses: List<ExistingQuestion>
) : TestingSession

@Serializable
data class ExistingTestingSession(
    val id: SerialUUID,
    override val userId: SerialUUID,
    override val testId: SerialUUID,
    override val questionResponses: List<ExistingQuestion>,
    val result: String? = null,
    val status: TestingSession.Status,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime? = null
) : TestingSession

sealed interface TestingSession {

    val userId: SerialUUID
    val testId: SerialUUID
    val questionResponses: List<ExistingQuestion>

    enum class Status {
        IN_PROGRESS,
        COMPLETED,
        CLOSED
    }

}
