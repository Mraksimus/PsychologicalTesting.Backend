package ru.psychologicalTesting.main.infrastructure.models

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object TokenModel : IdTable<UUID>("token") {

    val userId = reference("user_id", UserModel, onDelete = ReferenceOption.CASCADE)
    val value = varchar("value", 64)
    val createdAt = datetime("created_at")
    val expiresAt = datetime("expires_at")

    override val id = userId

}
