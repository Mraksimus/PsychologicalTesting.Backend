package ru.psychologicalTesting.common.testing.test

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
data class NewTest(
    override val name: String,
    override val description: String,
    override val category: TestCategory,
    override val transcript: String,
    override val questionsCount: Int,
    override val durationMins: String,
    override val isActive: Boolean
) : Test

@Serializable
data class ExistingTest(
    val id: SerialUUID,
    override val name: String,
    override val description: String,
    override val category: TestCategory,
    override val transcript: String,
    override val questionsCount: Int,
    override val durationMins: String,
    override val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val position: Int
) : Test

sealed interface Test {
    val name: String
    val description: String
    val category: TestCategory
    val transcript: String
    val questionsCount: Int
    val durationMins: String
    val isActive: Boolean
}
