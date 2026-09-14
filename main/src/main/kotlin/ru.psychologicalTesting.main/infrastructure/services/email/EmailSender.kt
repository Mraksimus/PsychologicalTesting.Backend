package ru.psychologicalTesting.main.infrastructure.services.email

interface EmailSender {
    val isEnabled: Boolean
    suspend fun send(to: String, subject: String, body: String): SendResult
}

sealed class SendResult {
    data object Success : SendResult()
    data object Disabled : SendResult()
    data class Failure(val cause: Throwable) : SendResult()
}
