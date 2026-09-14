package ru.psychologicalTesting.main.infrastructure.services.authentication.results

import ru.psychologicalTesting.main.infrastructure.dto.authentication.ExistingSession

sealed class CredentialValidationResult {

    data object Invalid : CredentialValidationResult()

    data class Success(
        val session: ExistingSession
    ) : CredentialValidationResult()

}
