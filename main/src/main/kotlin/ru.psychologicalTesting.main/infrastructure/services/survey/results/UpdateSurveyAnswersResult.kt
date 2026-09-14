package ru.psychologicalTesting.main.infrastructure.services.survey.results

sealed class UpdateSurveyAnswersResult {

    sealed class Error : UpdateSurveyAnswersResult()

    data object SessionNotFound : Error()

    data object SessionClosed : Error()

    data object AnswerWasNotUpdated : Error()

    data object Success : UpdateSurveyAnswersResult()

}
