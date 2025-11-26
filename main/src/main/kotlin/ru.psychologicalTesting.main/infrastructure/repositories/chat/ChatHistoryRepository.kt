package ru.psychologicalTesting.main.infrastructure.repositories.chat

import ru.psychologicalTesting.common.messages.LLMMessage
import java.util.*

interface ChatHistoryRepository {

    fun create(
        userId: UUID,
        message: String,
        role: LLMMessage.Role,
    ): LLMMessage

    fun findAllByUserId(userId: UUID): List<LLMMessage>

    fun clear(userId: UUID): Boolean

}
