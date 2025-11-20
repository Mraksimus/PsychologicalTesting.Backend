package ru.psychologicalTesting.main.infrastructure.controllers.chat

import dev.h4kt.ktorDocs.dsl.get
import dev.h4kt.ktorDocs.dsl.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.common.messages.LLMMessage
import ru.psychologicalTesting.common.types.LLMChatResponse
import ru.psychologicalTesting.main.infrastructure.controllers.chat.requests.ChatRequest
import ru.psychologicalTesting.main.infrastructure.repositories.chat.ChatHistoryRepository
import ru.psychologicalTesting.main.infrastructure.services.llm.LLMService
import ru.psychologicalTesting.main.infrastructure.services.llm.results.PromptResult
import ru.psychologicalTesting.main.plugins.authentication.UserPrincipal

fun Routing.configureChatRouting() = route("/chat") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    val llmService by inject<LLMService>()
    val chatHistoryRepository by inject<ChatHistoryRepository>()

    post {

        description = "Send a message to the chat"
        tags = listOf("Chat with LLM")

        requestBody = typeInfo<ChatRequest>()

        responses {
            HttpStatusCode.OK returns typeInfo<LLMChatResponse>()
            HttpStatusCode.BadRequest returns typeInfo<String>()
        }

        handle {

            val (message) = call.receive<ChatRequest>()

            val (userId) = call.principal<UserPrincipal>()!!

            val result = llmService.sendPrompt(
                userId = userId,
                prompt = message
            )

            when (result) {
                is PromptResult.Error ->
                    call.respond(HttpStatusCode.BadRequest, result.message)
                is PromptResult.Success ->
                    call.respond(HttpStatusCode.OK, result)
            }
        }

    }

    get {

        description = "Get all messages"
        tags = listOf("Chat with LLM")

        responses {
            HttpStatusCode.OK returns typeInfo<List<LLMMessage>>()
        }

        handle {

            val (userId) = call.principal<UserPrincipal>()!!

            val result = transaction {
                chatHistoryRepository.findAllByUserId(userId)
            }

            call.respond(HttpStatusCode.OK, result)
        }

    }

}
