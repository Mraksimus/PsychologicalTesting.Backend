package ru.psychologicalTesting.common.testing.session

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
@SerialName("SessionAnswer")
data class SessionAnswer(
    val questionId: SerialUUID,
    val selectedIndex: Int? = null,
    val selectedIndices: List<Int>? = null,
    val textAnswer: String? = null
)
