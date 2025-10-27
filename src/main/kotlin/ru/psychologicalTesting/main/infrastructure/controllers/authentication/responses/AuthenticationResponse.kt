package ru.psychologicalTesting.main.infrastructure.controllers.authentication.responses

import ru.psychologicalTesting.main.infrastructure.dto.Token

data class AuthenticationResponse(
    val token: Token
)
