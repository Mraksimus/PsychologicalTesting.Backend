package ru.psychologicalTesting.main.infrastructure.models.testing

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ru.psychologicalTesting.common.testing.test.TestCategory

object TestModel : UUIDTable("test") {
    val name = text("name")
    val description = text("description")
    val category = enumerationByName<TestCategory>("category", 13).default(TestCategory.EMOTIONS)
    val transcript = text("transcript")
    val questionsCount = integer("questions_count").default(0)
    val durationMins = text("duration_mins")
    val isActive = bool("is_active")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val position = integer("position")
}
