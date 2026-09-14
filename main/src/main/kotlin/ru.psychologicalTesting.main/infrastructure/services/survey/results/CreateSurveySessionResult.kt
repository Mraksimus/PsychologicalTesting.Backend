package ru.psychologicalTesting.main.infrastructure.services.survey.results

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.survey.session.FullSurveySession

sealed class CreateSurveySessionResult {

    sealed class Error : CreateSurveySessionResult()

    data object SurveyNotFound : Error()

    data object SurveyNotActive : Error()

    data object SurveyAlreadyStarted : Error()

    @Serializable
    data class Success(
        val createdSession: FullSurveySession
    ) : CreateSurveySessionResult()

}
