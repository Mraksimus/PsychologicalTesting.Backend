package ru.psychologicalTesting.main.infrastructure.services.llm

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.messages.LLMMessage
import ru.psychologicalTesting.common.types.LLMChatRequest
import ru.psychologicalTesting.common.types.LLMChatResponse
import ru.psychologicalTesting.main.config.llm.LLMConfig
import ru.psychologicalTesting.main.infrastructure.repositories.chat.ChatHistoryRepository
import ru.psychologicalTesting.main.infrastructure.services.llm.results.PromptResult
import java.util.*

@Single
class DefaultLLMService(
    private val chatRepository: ChatHistoryRepository,
    llmConfig: LLMConfig,
) : LLMService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    val llmServiceUrl = "http://${llmConfig.host}:${llmConfig.port}"

    override suspend fun sendPrompt(
        userId: UUID,
        prompt: String
    ): PromptResult {

        val chatHistory = transaction {
            chatRepository.findAllByUserId(userId)
        }

        val responseBody = LLMChatRequest(
            prompt = prompt,
            messages = chatHistory
        )

        val response = try {

            val result: LLMChatResponse = client.post("$llmServiceUrl/ollama/chat") {
                contentType(ContentType.Application.Json)
                setBody(responseBody)
            }.body()

            PromptResult.Success(result)
        } catch (ex: Exception) {
            PromptResult.Error(ex.message ?: "Unknown error")
        }

        if (response is PromptResult.Error) {
            return PromptResult.Error(response.message)
        } else if (response is PromptResult.Success) {
            transaction {

                chatRepository.create(
                    userId = userId,
                    message = prompt,
                    role = LLMMessage.Role.USER
                )

                chatRepository.create(
                    userId = userId,
                    message = response.llmChatResponse.message,
                    role = LLMMessage.Role.ASSISTANT
                )

            }
        }

        return response
    }

}
