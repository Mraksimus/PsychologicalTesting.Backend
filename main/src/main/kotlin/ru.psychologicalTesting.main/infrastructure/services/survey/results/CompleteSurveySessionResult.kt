package ru.psychologicalTesting.main.infrastructure.services.survey.results

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.survey.session.FullSurveySession

sealed class CompleteSurveySessionResult {

    sealed class Error : CompleteSurveySessionResult()

    data object SessionNotFound : Error()

    data object SurveyNotFound : Error()

    data object SessionMustBeOpened : Error()

    data object SurveyIsNotCompleted : Error()

    data object SessionUpdateError : Error()

    @Serializable
    data class Success(
        val session: FullSurveySession
    ) : CompleteSurveySessionResult()

}
