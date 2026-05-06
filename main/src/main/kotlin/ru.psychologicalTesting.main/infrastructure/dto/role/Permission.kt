package ru.psychologicalTesting.main.infrastructure.dto.role

import kotlinx.serialization.Serializable

@Serializable
enum class Permission {
    ADMIN, // Grants all other permissions
    ROLES_VIEW,
    ROLES_EDIT,
    TESTS_EDIT,
    QUESTIONS_EDIT,
    SESSIONS_VIEW
}
