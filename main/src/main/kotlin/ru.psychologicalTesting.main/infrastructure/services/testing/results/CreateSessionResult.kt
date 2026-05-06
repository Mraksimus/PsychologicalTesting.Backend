package ru.psychologicalTesting.main.infrastructure.services.testing.results

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.session.FullTestingSession

sealed class CreateSessionResult {

    sealed class Error : CreateSessionResult()

    data object TestNotFound : Error()

    data object TestAlreadyStarted : Error()

    @Serializable
    data class Success(
        val createdSession: FullTestingSession
    ) : CreateSessionResult()

}
