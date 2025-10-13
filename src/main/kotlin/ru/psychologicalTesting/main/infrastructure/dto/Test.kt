package ru.psychologicalTesting.main.infrastructure.dto

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.compat.SerialUUID

@Serializable
data class Test(
    val id: SerialUUID? = null,
    val value: String
)
