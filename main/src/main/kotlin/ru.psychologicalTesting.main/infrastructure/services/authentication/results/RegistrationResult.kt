package ru.psychologicalTesting.main.infrastructure.services.authentication.results

import ru.psychologicalTesting.main.infrastructure.dto.authentication.ExistingSession

sealed class RegistrationResult {

    data class Success(
        val session: ExistingSession,
        val token: String
    ) : RegistrationResult()

}
