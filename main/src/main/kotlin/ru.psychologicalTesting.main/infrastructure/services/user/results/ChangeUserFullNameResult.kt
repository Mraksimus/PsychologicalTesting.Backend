package ru.psychologicalTesting.main.infrastructure.services.user.results

sealed class ChangeUserFullNameResult {

    sealed class Error : ChangeUserFullNameResult()

    object UserNotFound : Error()

    object UserFullNameWasNotUpdated : Error()

    data object Success : ChangeUserFullNameResult()

}