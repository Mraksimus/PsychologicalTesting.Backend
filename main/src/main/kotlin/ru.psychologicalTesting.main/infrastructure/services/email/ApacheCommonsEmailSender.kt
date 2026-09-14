package ru.psychologicalTesting.main.infrastructure.services.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.mail.SimpleEmail
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import ru.psychologicalTesting.main.config.email.EmailConfig

@Single
class ApacheCommonsEmailSender(
    private val config: EmailConfig,
) : EmailSender {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val isEnabled: Boolean
        get() = config.isEnabled

    override suspend fun send(to: String, subject: String, body: String): SendResult {
        if (!config.isEnabled) {
            logger.info("Email disabled — would send to {} subject={}", to, subject)
            return SendResult.Disabled
        }
        return withContext(Dispatchers.IO) {
            runCatching {
                SimpleEmail().apply {
                    hostName = config.host
                    setSmtpPort(config.port)
                    setAuthentication(config.username, config.password)
                    isStartTLSEnabled = config.useStartTLS
                    setFrom(config.fromAddress.ifBlank { config.username }, config.fromName)
                    addTo(to)
                    this.subject = subject
                    setMsg(body)
                }.send()
                SendResult.Success as SendResult
            }.getOrElse { ex ->
                logger.error("Failed to send email to {}", to, ex)
                SendResult.Failure(ex)
            }
        }
    }
}
