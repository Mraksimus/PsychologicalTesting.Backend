package ru.psychologicalTesting.main.infrastructure.controllers.testing.session.requests

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.session.SessionAnswer

@Serializable
data class UpdateAnswersSessionRequest(
    val answers: List<SessionAnswer>
)
