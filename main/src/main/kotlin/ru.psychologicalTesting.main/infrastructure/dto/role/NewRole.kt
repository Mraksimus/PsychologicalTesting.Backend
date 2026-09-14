package ru.psychologicalTesting.main.infrastructure.dto.role

import dev.nesk.akkurate.annotations.Validate
import kotlinx.serialization.Serializable

@Validate
@Serializable
data class NewRole(
    override val name: String,
    override val color: String? = null,
    override val permissions: List<Permission> = emptyList()
) : Role
