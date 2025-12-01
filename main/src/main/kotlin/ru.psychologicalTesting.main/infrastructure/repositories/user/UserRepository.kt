package ru.psychologicalTesting.main.infrastructure.repositories.user

import ru.psychologicalTesting.main.infrastructure.dto.user.User
import java.util.*

interface UserRepository {

    fun create(
        name: String,
        surname: String,
        patronymic: String?,
        email: String,
        password: String
    ): User

    fun findByEmail(
        email: String
    ): User?

    fun findByUserId(
        userId: UUID
    ): User?

    fun update(
        user: User
    ): Boolean

    fun deleteById(
        userId: UUID
    ): Boolean

}
