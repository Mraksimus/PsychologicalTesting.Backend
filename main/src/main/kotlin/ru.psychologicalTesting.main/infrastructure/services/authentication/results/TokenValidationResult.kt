package ru.psychologicalTesting.main.infrastructure.services.authentication.results

import ru.psychologicalTesting.main.compat.SerialUUID

sealed class TokenValidationResult {

    data object Invalid : TokenValidationResult()

    data class Success(
        val userId: SerialUUID
    ) : TokenValidationResult()

}
