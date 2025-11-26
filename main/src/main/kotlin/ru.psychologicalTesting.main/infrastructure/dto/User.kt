package ru.psychologicalTesting.main.infrastructure.dto

import java.util.*

data class User(
    val id: UUID,
    val name: String,
    val surname: String,
    val patronymic: String? = null,
    val email: String,
    val password: String
)
