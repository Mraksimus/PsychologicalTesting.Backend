package ru.psychologicalTesting.main.infrastructure.repositories.user

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.infrastructure.dto.user.User
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class ExposedUserRepository : UserRepository {

    override fun create(
        name: String,
        surname: String,
        patronymic: String?,
        email: String,
        password: String
    ): User {

        val insertedRow = UserModel.insert {
            it[this.name] = name
            it[this.surname] = surname
            it[this.patronymic] = patronymic
            it[this.email] = email
            it[this.password] = password
            it[this.registeredAt] = LocalDateTime.now()
            it[this.lastLoginAt] = null
        }

        return User(
            id = insertedRow[UserModel.id].value,
            name = insertedRow[UserModel.name],
            surname = insertedRow[UserModel.surname],
            patronymic = insertedRow[UserModel.patronymic],
            email = insertedRow[UserModel.email],
            password = insertedRow[UserModel.password],
            registeredAt = insertedRow[UserModel.registeredAt],
            lastLoginAt = insertedRow[UserModel.lastLoginAt]
        )
    }

    override fun findByEmail(
        email: String
    ): User? {
        return UserModel
            .selectAll()
            .where(UserModel.email eq email)
            .firstOrNull()
            ?.toUser()
    }

    override fun findByUserId(
        userId: UUID
    ): User? {
        return UserModel
            .selectAll()
            .where(UserModel.id eq userId)
            .firstOrNull()
            ?.toUser()
    }

    override fun update(
        user: User
    ): Boolean {

        val affectedRow = UserModel
            .update(
                where = { UserModel.id eq user.id },
            ) {
                it[name] = user.name
                it[surname] = user.surname
                it[patronymic] = user.patronymic
                it[email] = user.email
                it[password] = user.password
                it[lastLoginAt] = user.lastLoginAt
            }

        return affectedRow > 0
    }

    override fun deleteById(
        userId: UUID
    ): Boolean {

        val affectedRow = UserModel.deleteById(userId)

        return affectedRow > 0
    }

    private fun ResultRow.toUser(): User = User(
        id = this[UserModel.id].value,
        name = this[UserModel.name],
        surname = this[UserModel.surname],
        patronymic = this[UserModel.patronymic],
        email = this[UserModel.email],
        password = this[UserModel.password],
        registeredAt = this[UserModel.registeredAt],
        lastLoginAt = this[UserModel.lastLoginAt]
    )

}
