package ru.psychologicalTesting.common.survey.survey

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.category.ExistingCategory
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
data class NewSurvey(
    override val name: String,
    override val description: String,
    override val durationMins: String,
    override val isActive: Boolean,
    override val categoryId: SerialUUID? = null,
) : Survey

@Serializable
data class ExistingSurvey(
    val id: SerialUUID,
    override val name: String,
    override val description: String,
    override val durationMins: String,
    override val isActive: Boolean,
    override val categoryId: SerialUUID? = null,
    val category: ExistingCategory? = null,
    val questionsCount: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val position: Int
) : Survey

sealed interface Survey {
    val name: String
    val description: String
    val durationMins: String
    val isActive: Boolean
    val categoryId: SerialUUID?
}
