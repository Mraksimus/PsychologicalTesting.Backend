package ru.psychologicalTesting.main.infrastructure.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.lowerCase
import ru.psychologicalTesting.main.utils.now

object UserModel : UUIDTable("user") {

    val name = text("name").default("")
    val surname = text("surname").default("")
    val patronymic = text("patronymic").nullable()
    val email = text("email")
    val password = text("password")
    val registeredAt = datetime("registered_at").default(LocalDateTime.now())
    val lastLoginAt = datetime("last_login_at").nullable()

    init {

        uniqueIndex(
            "unique_email_lowercase",
            functions = listOf(email.lowerCase())
        )

    }

}
