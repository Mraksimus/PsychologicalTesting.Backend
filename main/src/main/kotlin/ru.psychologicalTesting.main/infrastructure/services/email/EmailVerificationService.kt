package ru.psychologicalTesting.main.infrastructure.services.email

import java.util.UUID

interface EmailVerificationService {

    suspend fun issueForUser(userId: UUID): IssueResult

    fun confirm(token: String): ConfirmResult

    sealed class IssueResult {
        data class Success(val token: String, val emailSent: Boolean) : IssueResult()
        data object UserNotFound : IssueResult()
        data object AlreadyVerified : IssueResult()
        data class Failed(val cause: Throwable) : IssueResult()
    }

    sealed class ConfirmResult {
        data object Success : ConfirmResult()
        data object TokenNotFound : ConfirmResult()
        data object TokenExpired : ConfirmResult()
        data object TokenAlreadyUsed : ConfirmResult()
    }
}
