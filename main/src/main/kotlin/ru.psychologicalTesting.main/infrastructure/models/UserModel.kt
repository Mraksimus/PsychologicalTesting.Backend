package ru.psychologicalTesting.main.infrastructure.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.lowerCase

object UserModel : UUIDTable("user") {

    val name = text("name").default("")
    val surname = text("surname").default("")
    val patronymic = text("patronymic").nullable()
    val email = text("email")
    val password = text("password")

    init {

        uniqueIndex(
            "unique_email_lowercase",
            functions = listOf(email.lowerCase())
        )

    }

}
