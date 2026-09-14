package ru.psychologicalTesting.main.infrastructure.dto.survey

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.survey.survey.ExistingSurvey
import ru.psychologicalTesting.common.testing.question.ExistingQuestion

@Serializable
data class SurveyDetails(
    val survey: ExistingSurvey,
    val questions: List<ExistingQuestion>
)
