package ru.psychologicalTesting.main.infrastructure.services.user

import ru.psychologicalTesting.main.infrastructure.dto.User
import java.util.*

interface UserService {

    fun create(
        email: String,
        password: String
    ): User

    fun delete(userId: UUID)

}
