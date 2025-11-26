package ru.psychologicalTesting.llm.infrastructure.controllers.ollama

import ai.koog.ktor.llm
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.ContentPart
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.common.messages.LLMMessage
import ru.psychologicalTesting.common.types.chat.LLMChatRequest
import ru.psychologicalTesting.common.types.LLMResponse
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

        val response = runBlocking {

            llm().execute(
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
                    contextLength = 32_000
                )
            )

        }

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
            session
        ) = call.receive<LLMTestTranscriptionRequest>()

        val response = runBlocking {

            llm().execute(
                prompt = prompt("test_transcription") {

                    system(ollamaConfig.testTranscriptionSystemPrompt)

                    system("Информация о тесте: ${Json.encodeToString(test)}")
                    system("Вопросы теста и варианты ответов: ${Json.encodeToString(questions)}")
                    system("Сессия пользователя с ответами на вопросы: ${Json.encodeToString(session)}")

                },
                model = LLModel(
                    provider = LLMProvider.Ollama,
                    id = ollamaConfig.testTranscriptionModel,
                    capabilities = listOf(),
                    contextLength = 32_000
                )
            )

        }

        val text = response.joinToString(separator = "") { it.content }
        call.respond(HttpStatusCode.OK, LLMResponse(text))
    }

}
