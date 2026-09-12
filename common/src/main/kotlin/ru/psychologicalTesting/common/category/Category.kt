package ru.psychologicalTesting.common.category

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
data class NewCategory(
    override val name: String,
    override val color: String,
    override val icon: String,
) : Category

@Serializable
data class ExistingCategory(
    val id: SerialUUID,
    override val name: String,
    override val color: String,
    override val icon: String,
    val position: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) : Category

sealed interface Category {
    val name: String
    val color: String
    val icon: String
}
