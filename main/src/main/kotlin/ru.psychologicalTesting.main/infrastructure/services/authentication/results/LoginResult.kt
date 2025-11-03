package ru.psychologicalTesting.main.infrastructure.services.authentication.results

import ru.psychologicalTesting.main.infrastructure.dto.Token

sealed class LoginResult {

    data object InvalidCredentials : LoginResult()

    data class Success(
        val token: Token
    ) : LoginResult()

}
