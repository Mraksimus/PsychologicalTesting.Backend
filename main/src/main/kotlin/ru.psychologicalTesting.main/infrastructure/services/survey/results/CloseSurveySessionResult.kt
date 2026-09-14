package ru.psychologicalTesting.main.infrastructure.services.survey.results

sealed class CloseSurveySessionResult {

    sealed class Error : CloseSurveySessionResult()

    data object SessionMustBeOpened : Error()

    data object SessionNotFound : Error()

    data object SessionWasNotClosed : Error()

    data object Success : CloseSurveySessionResult()

}
