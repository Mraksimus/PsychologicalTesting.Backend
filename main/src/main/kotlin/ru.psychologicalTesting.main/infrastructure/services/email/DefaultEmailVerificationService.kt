package ru.psychologicalTesting.main.infrastructure.services.email

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.config.email.EmailConfig
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.models.EmailVerificationTokenModel
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import ru.psychologicalTesting.main.plugins.suspendedTransaction
import ru.psychologicalTesting.main.utils.now
import ru.psychologicalTesting.main.utils.plus
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import kotlin.time.Duration.Companion.hours

private const val TOKEN_BYTES = 32

@Single
class DefaultEmailVerificationService(
    private val emailSender: EmailSender,
    private val config: EmailConfig,
) : EmailVerificationService {

    private val random = SecureRandom()
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    override suspend fun issueForUser(userId: UUID): EmailVerificationService.IssueResult {

        val userRow = suspendedTransaction {
            UserModel
                .selectAll()
                .where(UserModel.id eq userId)
                .firstOrNull()
        } ?: return EmailVerificationService.IssueResult.UserNotFound

        if (userRow[UserModel.emailVerifiedAt] != null) {
            return EmailVerificationService.IssueResult.AlreadyVerified
        }

        val email = userRow[UserModel.email]
        val name = userRow[UserModel.name]
        val token = generateToken()
        val createdAt = LocalDateTime.now()
        val expiresAt = createdAt + config.tokenTtlHours.hours

        suspendedTransaction {
            EmailVerificationTokenModel.deleteWhere {
                EmailVerificationTokenModel.userId eq userId
            }
            EmailVerificationTokenModel.insert {
                it[EmailVerificationTokenModel.userId] = userId
                it[EmailVerificationTokenModel.token] = token
                it[EmailVerificationTokenModel.createdAt] = createdAt
                it[EmailVerificationTokenModel.expiresAt] = expiresAt
            }
        }

        val link = buildLink(token)
        val subject = "Подтверждение email · MindCheck"
        val body = buildString {
            appendLine("Здравствуйте, ${name.ifBlank { "пользователь" }}!")
            appendLine()
            appendLine("Чтобы подтвердить адрес $email, перейдите по ссылке:")
            appendLine(link)
            appendLine()
            appendLine("Ссылка действительна ${config.tokenTtlHours} ч.")
            appendLine("Если вы не создавали аккаунт, просто проигнорируйте это письмо.")
        }

        val sendResult = emailSender.send(email, subject, body)

        return when (sendResult) {
            is SendResult.Success -> EmailVerificationService.IssueResult.Success(token, true)
            is SendResult.Disabled -> EmailVerificationService.IssueResult.Success(token, false)
            is SendResult.Failure -> EmailVerificationService.IssueResult.Failed(sendResult.cause)
        }
    }

    override fun confirm(token: String): EmailVerificationService.ConfirmResult {

        val row = EmailVerificationTokenModel
            .selectAll()
            .where(EmailVerificationTokenModel.token eq token)
            .firstOrNull()
            ?: return EmailVerificationService.ConfirmResult.TokenNotFound

        val userId = row[EmailVerificationTokenModel.userId].value
        val now = LocalDateTime.now()

        if (row[EmailVerificationTokenModel.usedAt] != null) {
            return EmailVerificationService.ConfirmResult.TokenAlreadyUsed
        }

        if (row[EmailVerificationTokenModel.expiresAt] < now) {
            return EmailVerificationService.ConfirmResult.TokenExpired
        }

        UserModel.updateById(userId) {
            it[emailVerifiedAt] = now
        }

        EmailVerificationTokenModel.update({
            EmailVerificationTokenModel.id eq row[EmailVerificationTokenModel.id]
        }) {
            it[usedAt] = now
        }

        return EmailVerificationService.ConfirmResult.Success
    }

    private fun generateToken(): String {
        val bytes = ByteArray(TOKEN_BYTES).also(random::nextBytes)
        return encoder.encodeToString(bytes)
    }

    private fun buildLink(token: String): String {
        val base = config.verificationBaseUrl.trimEnd('/', '?', '&')
        val encoded = URLEncoder.encode(token, StandardCharsets.UTF_8)
        val separator = if (base.contains('?')) '&' else '?'
        return "$base${separator}token=$encoded"
    }

}
