package ru.psychologicalTesting.common.testing.question

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.testing.question.answer.Answer
import ru.psychologicalTesting.common.testing.question.answer.ClientAnswer
import ru.psychologicalTesting.common.testing.question.answer.FullAnswer
import ru.psychologicalTesting.common.testing.question.answer.toClientAnswer

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
            MULTIPLE,
            BINARY,
            SCALE
        }

    }

//    @Serializable
//    data class Input(
//        val text: String,
//        val correctInputs: List<String>? = null
//    ) : QuestionContentType()

}

fun QuestionContentType.toClient(): QuestionContentType {
    return when (this) {
        is QuestionContentType.Choice -> this.copy(
            options = options.map { answer ->
                when (answer) {
                    is FullAnswer -> answer.toClientAnswer()
                    is ClientAnswer -> answer
                }
            }
        )
    }
}

//    @Serializable
//    sealed class QuestionContentType {
//
//        @Serializable
//        data class SingleChoice(
//        val text: String,
//        val options: List<Answer>
//        ) : QuestionContentType()
//
//        @Serializable
//        data class MultipleChoice(
//        val text: String,
//        val options: List<Answer>
//        ) : QuestionContentType()
//
//        @Serializable
//        data class Binary(
//        val text: String,
//        val positiveLabel: String,
//        val negativeLabel: String
//        ) : QuestionContentType()
//
//        @Serializable
//        data class Scale(
//        val text: String,
//        val scaleSize: Int,
//        val labels: List<String>
//        ) : QuestionContentType()
//
//        @Serializable
//        data class Input(
//        val text: String,
//        val correctInputs: List<String>? = null
//        ) : QuestionContentType()
//
//    }
