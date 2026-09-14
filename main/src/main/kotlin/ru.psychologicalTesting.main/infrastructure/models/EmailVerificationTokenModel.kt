package ru.psychologicalTesting.main.infrastructure.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object EmailVerificationTokenModel : UUIDTable("email_verification_token") {
    val userId = reference("user_id", UserModel, onDelete = ReferenceOption.CASCADE)
    val token = text("token").uniqueIndex()
    val createdAt = datetime("created_at")
    val expiresAt = datetime("expires_at")
    val usedAt = datetime("used_at").nullable()
}
