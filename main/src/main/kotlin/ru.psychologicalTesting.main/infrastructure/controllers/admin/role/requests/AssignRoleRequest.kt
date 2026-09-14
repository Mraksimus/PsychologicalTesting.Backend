package ru.psychologicalTesting.main.infrastructure.controllers.admin.role.requests

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
data class AssignRoleRequest(
    val roleId: SerialUUID? = null
)
