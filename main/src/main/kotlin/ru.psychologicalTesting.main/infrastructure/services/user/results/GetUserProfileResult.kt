package ru.psychologicalTesting.main.infrastructure.services.user.results

import ru.psychologicalTesting.main.infrastructure.dto.user.UserProfile

sealed class GetUserProfileResult {

    sealed class Error : GetUserProfileResult()

    object UserNotFound : Error()

    data class Success(
        val userProfile: UserProfile
    ) : GetUserProfileResult()

}