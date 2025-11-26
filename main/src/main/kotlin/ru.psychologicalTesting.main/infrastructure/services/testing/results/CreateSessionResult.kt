package ru.psychologicalTesting.main.infrastructure.services.testing.results

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.infrastructure.dto.testing.session.ExistingTestingSession

sealed class CreateSessionResult {

    sealed class Error : CreateSessionResult()

    data object TestNotFound : Error()

    data object TestAlreadyStarted : Error()

    @Serializable
    data class Success(
        val createdSession: ExistingTestingSession
    ) : CreateSessionResult()

}
