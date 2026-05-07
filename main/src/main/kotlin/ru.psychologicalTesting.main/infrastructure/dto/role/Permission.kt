package ru.psychologicalTesting.main.infrastructure.dto.role

import kotlinx.serialization.Serializable

@Serializable
enum class Permission {
    ADMIN, // Grants all other permissions
    ROLES_VIEW,
    ROLES_EDIT,
    TESTS_VIEW,
    TESTS_EDIT,
    QUESTIONS_EDIT,
    USERS_VIEW,
    SESSIONS_VIEW
}
