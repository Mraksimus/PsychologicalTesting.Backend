package ru.psychologicalTesting.main.infrastructure.controllers.testing.session.requests

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.question.ExistingQuestion

@Serializable
data class UpdateAnswersSessionRequest(
    val questionResponses: List<ExistingQuestion>
)
