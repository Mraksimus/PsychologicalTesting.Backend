package ru.psychologicalTesting.main.infrastructure.repositories.authentication

import ru.psychologicalTesting.main.infrastructure.dto.Token
import java.util.UUID

interface TokenRepository {

    fun create(dto: Token): Token

    fun findOneByValue(
        token: String
    ): Token?

    fun update(
        userId: UUID,
        dto: Token
    ): Boolean

    fun delete(id: UUID)

}
