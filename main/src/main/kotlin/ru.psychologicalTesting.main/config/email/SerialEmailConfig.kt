package ru.psychologicalTesting.main.config.email

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SerialEmailConfig(
    @SerialName("enabled")
    override val isEnabled: Boolean = false,
    override val host: String = "",
    override val port: Int = 0,
    override val username: String = "",
    override val password: String = "",
    override val useStartTLS: Boolean = false,
    override val fromAddress: String = "",
    override val fromName: String = "MindCheck",
    override val verificationBaseUrl: String = "http://localhost:5173/verify-email",
    override val tokenTtlHours: Int = 24,
) : EmailConfig
