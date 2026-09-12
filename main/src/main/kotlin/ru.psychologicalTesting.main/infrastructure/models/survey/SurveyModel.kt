package ru.psychologicalTesting.main.infrastructure.models.survey

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ru.psychologicalTesting.main.infrastructure.models.category.CategoryModel

object SurveyModel : UUIDTable("survey") {
    val name = text("name")
    val description = text("description")
    val durationMins = text("duration_mins")
    val isActive = bool("is_active")
    val categoryId = reference("category_id", CategoryModel, onDelete = ReferenceOption.SET_NULL).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val position = integer("position")
}
