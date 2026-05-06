package ru.psychologicalTesting.common.testing.question.answer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Answer")
data class Answer(
    val index: Int,
    val text: String,
    val score: Int
)
