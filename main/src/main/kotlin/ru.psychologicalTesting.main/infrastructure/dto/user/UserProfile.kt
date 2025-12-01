package ru.psychologicalTesting.main.infrastructure.dto.user

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String,
    val surname: String,
    val patronymic: String? = null,
    val email: String,
    val registeredAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null
)
