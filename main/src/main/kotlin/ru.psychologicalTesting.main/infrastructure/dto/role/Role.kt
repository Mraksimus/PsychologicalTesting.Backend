package ru.psychologicalTesting.main.infrastructure.dto.role

import kotlinx.serialization.Serializable

@Serializable
sealed interface Role {
    val name: String
    val color: String?
    val permissions: List<Permission>
}
