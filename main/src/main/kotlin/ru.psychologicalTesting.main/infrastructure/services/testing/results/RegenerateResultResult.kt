package ru.psychologicalTesting.main.infrastructure.services.testing.results

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.session.FullTestingSession

sealed class RegenerateResultResult {

    sealed class Error : RegenerateResultResult()

    data object SessionNotFound : Error()

    data object SessionNotCompleted : Error()

    data object TestNotFound : Error()

    data object LLMRequestError : Error()

    data object SessionUpdateError : Error()

    @Serializable
    data class Success(
        val session: FullTestingSession
    ) : RegenerateResultResult()
}
