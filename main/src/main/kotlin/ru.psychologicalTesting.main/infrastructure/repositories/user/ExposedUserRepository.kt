package ru.psychologicalTesting.main.infrastructure.repositories.user

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.infrastructure.dto.User
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import java.util.*

@Single
class ExposedUserRepository : UserRepository {

    override fun create(
        email: String,
        password: String
    ): User {

        val insertedRow = UserModel.insert {
            it[this.email] = email
            it[this.password] = password
        }

        return User(
            id = insertedRow[UserModel.id].value,
            email = insertedRow[UserModel.email],
            password = insertedRow[UserModel.password]
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

    override fun updateById(
        user: User
    ): Boolean {

        val affectedRow = UserModel
            .update(
                where = { UserModel.id eq user.id },
            ) {
                it[email] = user.email
                it[password] = user.password
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
        email = this[UserModel.email],
        password = this[UserModel.password]
    )

}
