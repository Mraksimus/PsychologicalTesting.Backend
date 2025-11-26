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
import ru.psychologicalTesting.common.testing.question.ExistingQuestion
import ru.psychologicalTesting.common.testing.session.ExistingTestingSession
import ru.psychologicalTesting.common.testing.test.ExistingTest
import ru.psychologicalTesting.common.types.chat.LLMChatRequest
import ru.psychologicalTesting.common.types.LLMResponse
import ru.psychologicalTesting.common.types.testTranscription.LLMTestTranscriptionRequest
import ru.psychologicalTesting.main.infrastructure.repositories.chat.ChatHistoryRepository
import ru.psychologicalTesting.main.infrastructure.services.llm.results.PromptResult
import ru.psychologicalTesting.main.plugins.suspendedTransaction
import java.util.*

@Single
class DefaultLLMService(
    private val chatRepository: ChatHistoryRepository
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

            val result: LLMResponse = client.post("http://localhost:1489/ollama/chat") {
                contentType(ContentType.Application.Json)
                setBody(responseBody)
            }.body()

            PromptResult.Success(result)
        } catch (_: Exception) {
            PromptResult.Error
        }

        if (response is PromptResult.Error) {
            return PromptResult.Error
        } else if (response is PromptResult.Success) {
            suspendedTransaction {

                chatRepository.create(
                    userId = userId,
                    message = prompt,
                    role = LLMMessage.Role.USER
                )

                chatRepository.create(
                    userId = userId,
                    message = response.llmResponse.message,
                    role = LLMMessage.Role.ASSISTANT
                )

            }
        }

        return response
    }

    override suspend fun sendTestResult(
        test: ExistingTest,
        questions: List<ExistingQuestion>,
        session: ExistingTestingSession
    ): PromptResult {

        val responseBody = LLMTestTranscriptionRequest(
            test = test,
            questions = questions,
            session = session
        )

        val response = try {

            val result: LLMResponse = client.post("http://localhost:1489/ollama/test") {
                contentType(ContentType.Application.Json)
                setBody(responseBody)
            }.body()

            PromptResult.Success(result)
        } catch (_: Exception) {
            PromptResult.Error
        }

        if (response is PromptResult.Error) {
            return PromptResult.Error
        }

        return response
    }

}
