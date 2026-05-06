package ru.psychologicalTesting.main.infrastructure.dto.role

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
data class ExistingRole(
    val id: SerialUUID,
    override val name: String,
    override val color: String?,
    override val permissions: List<Permission>
) : Role
