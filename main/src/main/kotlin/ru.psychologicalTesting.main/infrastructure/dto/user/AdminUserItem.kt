package ru.psychologicalTesting.main.infrastructure.dto.user

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID
import ru.psychologicalTesting.main.infrastructure.dto.role.ExistingRole

@Serializable
data class AdminUserItem(
    val id: SerialUUID,
    val name: String,
    val surname: String,
    val patronymic: String? = null,
    val email: String,
    val role: ExistingRole? = null,
    val registeredAt: LocalDateTime,
    val lastLoginAt: LocalDateTime? = null
)
