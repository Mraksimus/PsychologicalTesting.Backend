package ru.psychologicalTesting.main.infrastructure.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object SessionModel : UUIDTable("session") {

    val userId = reference("user_id", UserModel, onDelete = ReferenceOption.CASCADE)
    val userAgent = text("user_agent")
    val ipAddress = varchar("ip_address", 45)
    val lastLoginAt = datetime("last_login_at")

}
