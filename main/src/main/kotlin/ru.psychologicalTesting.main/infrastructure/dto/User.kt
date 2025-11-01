package ru.psychologicalTesting.main.infrastructure.dto

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val password: String
)
