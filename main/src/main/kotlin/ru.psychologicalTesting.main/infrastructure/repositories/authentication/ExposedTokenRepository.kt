package ru.psychologicalTesting.main.infrastructure.repositories.authentication

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.Token
import ru.psychologicalTesting.main.infrastructure.models.TokenModel
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class ExposedTokenRepository : TokenRepository {

    override fun create(
        dto: Token
    ): Token {

        val insertedRow = TokenModel.insert {
            it[userId] = dto.userId
            it[value] = dto.value
            it[createdAt] = dto.createdAt
            it[expiresAt] = dto.expiresAt
        }

        return dto.copy(userId = insertedRow[TokenModel.userId].value)
    }

    override fun findOneByValue(token: String): Token? {
        return TokenModel
            .selectAll()
            .where(TokenModel.value eq token)
            .firstOrNull()
            ?.toTokenDto()
    }

    override fun update(
        userId: UUID,
        dto: Token
    ): Boolean {

        val affectedRows = TokenModel.updateById(userId) {
            it[value] = dto.value
            it[createdAt] = dto.createdAt
            it[expiresAt] = dto.expiresAt
        }

        return affectedRows > 0
    }

    override fun clearExpired(): Boolean {
        return TokenModel.deleteWhere {
            TokenModel.expiresAt lessEq LocalDateTime.now()
        } > 0
    }

    override fun delete(id: UUID) {
        TokenModel.deleteById(id)
    }

    private fun ResultRow.toTokenDto() = Token(
        userId = this[TokenModel.userId].value,
        value = this[TokenModel.value],
        createdAt = this[TokenModel.createdAt],
        expiresAt = this[TokenModel.expiresAt]
    )

}
