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
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.common.messages.LLMMessage
import ru.psychologicalTesting.common.types.LLMChatRequest
import ru.psychologicalTesting.common.types.LLMChatResponse
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
     * @description Обрабатывает пользовательское сообщение через ReAct стратегию с моделью Qwen3:14B
     * @request ChatRequest - объект с пользовательским сообщением
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

                    system(ollamaConfig.systemPrompt)

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
                    id = ollamaConfig.model,
                    capabilities = listOf(),
                    contextLength = 32_000
                )
            )

        }

        val text = response.joinToString(separator = "") { it.content }
        call.respond(HttpStatusCode.OK, LLMChatResponse(text))
    }

}
