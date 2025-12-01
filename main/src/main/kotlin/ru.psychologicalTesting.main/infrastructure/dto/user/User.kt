package ru.psychologicalTesting.main.infrastructure.dto.user

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
data class User(
    val id: SerialUUID,
    val name: String,
    val surname: String,
    val patronymic: String? = null,
    val email: String,
    val password: String,
    val registeredAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null
)
