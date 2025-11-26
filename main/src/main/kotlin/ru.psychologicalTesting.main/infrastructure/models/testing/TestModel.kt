package ru.psychologicalTesting.main.infrastructure.models.testing

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object TestModel : UUIDTable("test") {
    val name = text("name")
    val description = text("description")
    val transcript = text("transcript")
    val durationMins = text("duration_mins")
    val isActive = bool("is_active")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val position = integer("position")
}
