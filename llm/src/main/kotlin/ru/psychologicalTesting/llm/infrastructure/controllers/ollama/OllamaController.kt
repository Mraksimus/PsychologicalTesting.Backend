package ru.psychologicalTesting.llm.infrastructure.controllers.ollama

import ai.koog.ktor.llm
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.ContentPart
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.params.LLMParams
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.Clock
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.common.messages.LLMMessage
import ru.psychologicalTesting.common.testing.question.ExistingQuestion
import ru.psychologicalTesting.common.testing.question.QuestionContentType
import ru.psychologicalTesting.common.testing.session.SessionAnswer
import ru.psychologicalTesting.common.types.LLMResponse
import ru.psychologicalTesting.common.types.chat.LLMChatRequest
import ru.psychologicalTesting.common.types.testTranscription.LLMTestTranscriptionRequest
import ru.psychologicalTesting.llm.config.ollama.OllamaConfig

fun Routing.configureOllamaRouting() = route("/ollama") {
    configureChatRoutes()
//    configureTestAnalysisRoutes()
}

private fun Route.configureChatRoutes() {

    val ollamaConfig by inject<OllamaConfig>()

    /**
     * @tags Chat
     * @summary Отправить сообщение и получить ответ от AI агента
     * @description Обрабатывает историю чата, сообщение пользователя и отвечает
     * @request LLMChatRequest - объект с сообщениями пользователя
     * @response 200: ChatResponse - успешный ответ с обработанным результатом
     * @response 400: Bad Request - некорректный формат запроса
     * @response 500: Internal Server Error - ошибка сервера
     */
    post("/chat") {

        val request = call.receive<LLMChatRequest>()

        val koogMessages = request.messages.map { messages ->
            when (messages.role) {
                LLMMessage.Role.SYSTEM -> Message.System(
                    parts = listOf(ContentPart.Text(messages.content)),
                    metaInfo = RequestMetaInfo(
                        timestamp = Clock.System.now()
                    )
                )
                LLMMessage.Role.USER -> Message.User(
                    parts = listOf(ContentPart.Text(messages.content)),
                    metaInfo = RequestMetaInfo(
                        timestamp = Clock.System.now()
                    )
                )
                LLMMessage.Role.ASSISTANT -> Message.Assistant(
                    parts = listOf(ContentPart.Text(messages.content)),
                    metaInfo = ResponseMetaInfo(
                        timestamp = Clock.System.now()
                    )
                )
            }
        }

        val response = llm().execute(
            prompt = prompt("chat") {

                system(ollamaConfig.chatSystemPrompt)

                request.messages.forEach { msg ->
                    when (msg.role) {
                        LLMMessage.Role.SYSTEM -> system(msg.content)
                        LLMMessage.Role.USER -> user(msg.content)
                        LLMMessage.Role.ASSISTANT -> assistant(msg.content)
                    }
                }

                user(request.prompt)

            },
            model = LLModel(
                provider = LLMProvider.Ollama,
                id = ollamaConfig.chatModel,
                capabilities = listOf(),
                contextLength = ollamaConfig.chatContext
            )
        )

        val text = response.joinToString(separator = "") { it.content }
        call.respond(HttpStatusCode.OK, LLMResponse(text))
    }

    /**
     * @tags Test
     * @summary Расшифровка результата тестирования
     * @description Обрабатывает тест, пройденный пользователем
     * @request LLMTestTranscriptionRequest - объект с информацией о тесте и его результатом
     * @response 200: ChatResponse - успешный ответ с расшифровкой
     * @response 400: Bad Request - некорректный формат запроса
     * @response 500: Internal Server Error - ошибка сервера
     */
    post("test") {

        val (
            test,
            questions,
            answers,
        ) = call.receive<LLMTestTranscriptionRequest>()

        val answersByQuestion = answers.associateBy { it.questionId }
        val sortedQuestions = questions.sortedBy { it.position }

        val questionsAndAnswers = buildString {
            sortedQuestions.forEachIndexed { idx, question ->
                val number = idx + 1
                appendLine("$number. ${questionText(question)}")
                appendLine("   Ответ: ${formatAnswer(question, answersByQuestion[question.id])}")
                appendLine()
            }
        }.trimEnd()

        val userPayload = buildString {
            appendLine("Название теста: ${test.name}")
            appendLine()
            appendLine("Инструкция по интерпретации (расшифровка):")
            appendLine(test.transcript.trim())
            appendLine()
            appendLine("Вопросы и ответы пользователя (в порядке нумерации теста):")
            appendLine(questionsAndAnswers)
        }

        val response = llm().execute(
            prompt = prompt(
                id = "test_transcription",
                params = LLMParams(
                    temperature = LLM_TEMPERATURE,
                    maxTokens = LLM_MAX_OUTPUT_TOKENS,
                )
            ) {
                system(ollamaConfig.testTranscriptionSystemPrompt)
                user(userPayload)
            },
            model = LLModel(
                provider = LLMProvider.Ollama,
                id = ollamaConfig.testTranscriptionModel,
                capabilities = listOf(),
                contextLength = ollamaConfig.testTranscriptionContext
            )
        )

        val text = response.joinToString(separator = "") { it.content }
        call.respond(HttpStatusCode.OK, LLMResponse(text))
    }

}

private const val LLM_TEMPERATURE: Double = 0.3
private const val LLM_MAX_OUTPUT_TOKENS: Int = 1500

private fun questionText(question: ExistingQuestion): String = when (val c = question.content) {
    is QuestionContentType.Choice -> c.text.normalizeWhitespace()
    is QuestionContentType.Input -> c.text.normalizeWhitespace()
}

private fun formatAnswer(
    question: ExistingQuestion,
    answer: SessionAnswer?
): String {
    if (answer == null) return "нет ответа"

    return when (val c = question.content) {
        is QuestionContentType.Choice -> {
            val indices = when {
                answer.selectedIndex != null -> listOf(answer.selectedIndex!!)
                !answer.selectedIndices.isNullOrEmpty() -> answer.selectedIndices!!
                else -> emptyList()
            }

            if (indices.isEmpty()) {
                "нет ответа"
            } else indices.joinToString(", ") { idx ->
                c.options.getOrNull(idx)?.text?.normalizeWhitespace() ?: "?"
            }
        }
        is QuestionContentType.Input -> {
            answer.textAnswer?.trim().takeUnless { it.isNullOrBlank() } ?: "нет ответа"
        }
    }
}

private fun String.normalizeWhitespace(): String =
    replace(Regex("\\s+"), " ").trim()
