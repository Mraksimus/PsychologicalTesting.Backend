package ru.psychologicalTesting.common.testing.question

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.question.answer.Answer

@Serializable
sealed class QuestionContentType {

    @Serializable
    @SerialName("Choice")
    data class Choice(
        val text: String,
        val mod: ChoiceMod,
        val options: List<Answer>
    ) : QuestionContentType() {

        enum class ChoiceMod {
            SINGLE,
            SCALE,
            MULTIPLE
        }

    }

    @Serializable
    @SerialName("Input")
    data class Input(
        val text: String,
        val correctInputs: List<String>? = null
    ) : QuestionContentType()

}
