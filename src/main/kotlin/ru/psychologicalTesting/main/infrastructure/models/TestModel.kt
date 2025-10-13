package ru.psychologicalTesting.main.infrastructure.models

import org.jetbrains.exposed.dao.id.UUIDTable

object TestModel : UUIDTable("test") {
    val value = varchar("value", 255)
}
