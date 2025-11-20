package ru.psychologicalTesting.main.infrastructure.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ru.psychologicalTesting.common.messages.LLMMessage

object ChatHistoryModel : UUIDTable("chat_history") {

    val userId = reference("user_id", UserModel, onDelete = ReferenceOption.CASCADE)
    val message = text("message")
    val role = enumerationByName<LLMMessage.Role>("role", 10)
    val createdAt = datetime("created_at")

}
