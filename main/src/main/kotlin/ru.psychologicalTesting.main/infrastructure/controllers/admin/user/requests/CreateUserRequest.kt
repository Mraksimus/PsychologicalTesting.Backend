package ru.psychologicalTesting.main.infrastructure.controllers.admin.user.requests

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
data class CreateUserRequest(
    val name: String,
    val surname: String,
    val patronymic: String? = null,
    val email: String,
    val password: String,
    val roleId: SerialUUID? = null,
)
