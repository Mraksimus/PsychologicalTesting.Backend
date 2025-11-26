package ru.psychologicalTesting.main.infrastructure.dto.testing.question.answer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("FullAnswer")
data class FullAnswer(
    override val index: Int,
    override val text: String,
    val isCorrect: Boolean? = null,
    val score: Int? = null
) : Answer

@Serializable
@SerialName("ClientAnswer")
data class ClientAnswer(
    override val index: Int,
    override val text: String,
    val isSelected: Boolean?,
) : Answer

@Serializable
sealed interface Answer {
    val index: Int
    val text: String
}

fun FullAnswer.toClientAnswer() = ClientAnswer(
    index = index,
    text = text,
    isSelected = null
)
