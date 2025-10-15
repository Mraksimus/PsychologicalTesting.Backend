package ru.psychologicalTesting.main.infrastructure.services.authentication.results

import ru.psychologicalTesting.main.infrastructure.dto.Token

sealed class RegistrationResult {

    data class Success(
        val token: Token
    ) : RegistrationResult()

}
