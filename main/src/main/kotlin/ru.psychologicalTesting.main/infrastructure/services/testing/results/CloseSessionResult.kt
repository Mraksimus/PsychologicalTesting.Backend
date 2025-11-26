package ru.psychologicalTesting.main.infrastructure.services.testing.results

sealed class CloseSessionResult {

    sealed class Error : CloseSessionResult()

    data object SessionMustBeOpened : Error()

    data object SessionNotFound : Error()

    data object SessionWasNotClosed : Error()

    data object Success : CloseSessionResult()

}
