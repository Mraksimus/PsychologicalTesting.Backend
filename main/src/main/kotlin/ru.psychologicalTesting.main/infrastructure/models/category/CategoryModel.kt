package ru.psychologicalTesting.main.infrastructure.models.category

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object CategoryModel : UUIDTable("category") {
    val name = text("name")
    val color = text("color")
    val icon = text("icon")
    val position = integer("position")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
