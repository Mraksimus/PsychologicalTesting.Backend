package ru.psychologicalTesting.main.infrastructure.dto.authentication

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

sealed interface Session {
    val userId: SerialUUID
    val userAgent: String
    val ipAddress: String
    val lastLoginAt: LocalDateTime
}

@Serializable
data class NewSession(
    override val userId: SerialUUID,
    override val userAgent: String,
    override val ipAddress: String,
    override val lastLoginAt: LocalDateTime
) : Session

@Serializable
data class ExistingSession(
    val id: SerialUUID,
    override val userId: SerialUUID,
    override val userAgent: String,
    override val ipAddress: String,
    override val lastLoginAt: LocalDateTime
) : Session
