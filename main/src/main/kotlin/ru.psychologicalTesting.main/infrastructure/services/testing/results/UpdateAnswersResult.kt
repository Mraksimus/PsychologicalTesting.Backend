package ru.psychologicalTesting.main.infrastructure.services.testing.results

sealed class UpdateAnswersResult {

    sealed class Error : UpdateAnswersResult()

    data object SessionNotFound : Error()

    data object SessionClosed : Error()

    data object AnswerWasNotUpdated : Error()

    data object Success : UpdateAnswersResult()

}
