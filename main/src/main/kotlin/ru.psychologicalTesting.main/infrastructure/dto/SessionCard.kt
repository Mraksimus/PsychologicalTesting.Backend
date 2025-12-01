package ru.psychologicalTesting.main.infrastructure.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID
import ru.psychologicalTesting.common.testing.session.TestingSession

@Serializable
data class TestingSessionCard(
    val id: SerialUUID,
    val testName: String,
    val status: TestingSession.Status,
    val createdAt: LocalDateTime
)
