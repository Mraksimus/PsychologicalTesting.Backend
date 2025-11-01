package ru.psychologicalTesting.main.infrastructure.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.compat.SerialUUID

@Serializable
data class Token(
    val userId: SerialUUID,
    val value: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime
)
