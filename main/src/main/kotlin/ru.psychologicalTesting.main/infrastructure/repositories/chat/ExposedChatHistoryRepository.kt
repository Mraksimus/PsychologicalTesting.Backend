package ru.psychologicalTesting.main.infrastructure.repositories.chat

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.messages.LLMMessage
import ru.psychologicalTesting.main.infrastructure.models.ChatHistoryModel
import ru.psychologicalTesting.main.utils.now
import java.util.UUID

@Single
class ExposedChatHistoryRepository : ChatHistoryRepository {

    override fun create(
        userId: UUID,
        message: String,
        role: LLMMessage.Role
    ): LLMMessage {

        val insertedRow = ChatHistoryModel.insert {
            it[this.userId] = userId
            it[this.message] = message
            it[this.role] = role
            it[createdAt] = LocalDateTime.now()
        }

        return LLMMessage(
            content = insertedRow[ChatHistoryModel.message],
            role = insertedRow[ChatHistoryModel.role]
        )
    }

    override fun findAllByUserId(
        userId: UUID
    ): List<LLMMessage> {
        return ChatHistoryModel
            .selectAll()
            .where(ChatHistoryModel.userId eq userId)
            .orderBy(ChatHistoryModel.createdAt, SortOrder.ASC)
            .map {
                LLMMessage(
                    content = it[ChatHistoryModel.message],
                    role = it[ChatHistoryModel.role]
                )
            }
    }

    override fun clear(
        userId: UUID
    ): Boolean {
        return ChatHistoryModel.deleteWhere {
            ChatHistoryModel.userId eq userId
        } > 0
    }

}
