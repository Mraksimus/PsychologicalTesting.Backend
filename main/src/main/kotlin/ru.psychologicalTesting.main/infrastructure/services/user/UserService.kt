package ru.psychologicalTesting.main.infrastructure.services.user

import ru.psychologicalTesting.main.infrastructure.dto.user.User
import ru.psychologicalTesting.main.infrastructure.services.user.results.ChangeUserFullNameResult
import ru.psychologicalTesting.main.infrastructure.services.user.results.GetUserProfileResult
import java.util.*

interface UserService {

    fun create(
        name: String,
        surname: String,
        patronymic: String?,
        email: String,
        password: String
    ): User

    fun getUserProfileById(userId: UUID): GetUserProfileResult

    fun changeUserFullName(
        userId: UUID,
        name: String,
        surname: String,
        patronymic: String?
    ): ChangeUserFullNameResult

    fun delete(userId: UUID)

}
