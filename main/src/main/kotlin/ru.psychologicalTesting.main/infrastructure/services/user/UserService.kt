package ru.psychologicalTesting.main.infrastructure.services.user

import ru.psychologicalTesting.main.infrastructure.dto.User
import java.util.*

interface UserService {

    fun create(
        name: String,
        surname: String,
        patronymic: String?,
        email: String,
        password: String
    ): User

    fun delete(userId: UUID)

}
