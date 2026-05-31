package ru.psychologicalTesting.main.infrastructure.controllers.authentication.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationResponse(
    val token: String
)
