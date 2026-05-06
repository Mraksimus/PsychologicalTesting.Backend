package ru.psychologicalTesting.main.plugins.authentication

import ru.psychologicalTesting.common.compat.SerialUUID
import ru.psychologicalTesting.main.infrastructure.dto.role.ExistingRole

data class UserPrincipal(
    val id: SerialUUID,
    val role: ExistingRole? = null
)
