package ru.psychologicalTesting.main.infrastructure.services.survey.results

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.survey.session.FullSurveySession

@Serializable
sealed class GetSurveySessionResult {

    data object Error : GetSurveySessionResult()

    @Serializable
    data class Success(
        val session: FullSurveySession
    ) : GetSurveySessionResult()

}
