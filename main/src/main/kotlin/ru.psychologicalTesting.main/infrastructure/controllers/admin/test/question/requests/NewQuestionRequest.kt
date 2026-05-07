package ru.psychologicalTesting.main.infrastructure.controllers.admin.test.question.requests

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.question.QuestionContentType

@Serializable
data class NewQuestionRequest(
    val content: QuestionContentType
)
