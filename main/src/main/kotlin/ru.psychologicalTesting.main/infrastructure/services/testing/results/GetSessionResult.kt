package ru.psychologicalTesting.main.infrastructure.services.testing.results

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.session.FullTestingSession

@Serializable
sealed class GetSessionResult {

    data object Error : GetSessionResult()

    @Serializable
    data class Success(
        val session: FullTestingSession
    ) : GetSessionResult()

}
