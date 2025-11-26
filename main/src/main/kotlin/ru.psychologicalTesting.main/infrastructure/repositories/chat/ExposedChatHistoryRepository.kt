package ru.psychologicalTesting.main.infrastructure.repositories.chat

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.messages.LLMMessage
import ru.psychologicalTesting.main.infrastructure.models.MessageModule
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class ExposedChatHistoryRepository : ChatHistoryRepository {

    override fun create(
        userId: UUID,
        message: String,
        role: LLMMessage.Role
    ): LLMMessage {

        val insertedRow = MessageModule.insert {
            it[this.userId] = userId
            it[this.message] = message
            it[this.role] = role
            it[createdAt] = LocalDateTime.now()
        }

        return LLMMessage(
            content = insertedRow[MessageModule.message],
            role = insertedRow[MessageModule.role]
        )
    }

    override fun findAllByUserId(
        userId: UUID
    ): List<LLMMessage> {
        return MessageModule
            .selectAll()
            .where(MessageModule.userId eq userId)
            .orderBy(MessageModule.createdAt, SortOrder.ASC)
            .map {
                LLMMessage(
                    content = it[MessageModule.message],
                    role = it[MessageModule.role]
                )
            }
    }

    override fun clear(
        userId: UUID
    ): Boolean {
        return MessageModule.deleteWhere {
            MessageModule.userId eq userId
        } > 0
    }

}
