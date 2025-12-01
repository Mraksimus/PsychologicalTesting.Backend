package ru.psychologicalTesting.main.infrastructure.dto.user

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String,
    val surname: String,
    val patronymic: String? = null,
    val email: String,
    val sessionsCount: Int,
    val completedSessionsCount: Int,
    val inProgressSessionsCount: Int,
    val registeredAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null
)
