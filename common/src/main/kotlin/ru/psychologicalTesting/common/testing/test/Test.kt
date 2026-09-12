package ru.psychologicalTesting.common.testing.test

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.category.ExistingCategory
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
data class NewTest(
    override val name: String,
    override val description: String,
    override val transcript: String,
    override val durationMins: String,
    override val isActive: Boolean,
    override val categoryId: SerialUUID? = null,
) : Test

@Serializable
data class ExistingTest(
    val id: SerialUUID,
    override val name: String,
    override val description: String,
    override val transcript: String,
    override val durationMins: String,
    override val isActive: Boolean,
    override val categoryId: SerialUUID? = null,
    val category: ExistingCategory? = null,
    val questionsCount: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val position: Int
) : Test

sealed interface Test {
    val name: String
    val description: String
    val transcript: String
    val durationMins: String
    val isActive: Boolean
    val categoryId: SerialUUID?
}
