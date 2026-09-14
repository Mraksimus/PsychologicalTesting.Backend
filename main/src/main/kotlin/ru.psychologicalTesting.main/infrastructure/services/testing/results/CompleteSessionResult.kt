package ru.psychologicalTesting.main.infrastructure.services.testing.results

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.session.FullTestingSession

sealed class CompleteSessionResult {

    sealed class Error : CompleteSessionResult()

    data object SessionNotFound : Error()

    data object TestNotFound : Error()

    data object SessionMustBeOpened : Error()

    data object TestIsNotCompleted : Error()

    data object LLMRequestError : Error()

    data object SessionUpdateError : Error()

    @Serializable
    data class Success(
        val session: FullTestingSession
    ) : CompleteSessionResult()

}
