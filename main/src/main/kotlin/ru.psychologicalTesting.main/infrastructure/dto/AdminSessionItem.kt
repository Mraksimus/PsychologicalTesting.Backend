package ru.psychologicalTesting.main.infrastructure.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID
import ru.psychologicalTesting.common.testing.session.TestingSession

@Serializable
data class AdminSessionItem(
    val id: SerialUUID,
    val userId: SerialUUID,
    val userFullName: String,
    val userEmail: String,
    val testId: SerialUUID,
    val status: TestingSession.Status,
    val result: String? = null,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime? = null
)
