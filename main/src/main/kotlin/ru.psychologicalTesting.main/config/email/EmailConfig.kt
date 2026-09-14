package ru.psychologicalTesting.main.config.email

interface EmailConfig {
    val isEnabled: Boolean
    val host: String
    val port: Int
    val username: String
    val password: String
    val useStartTLS: Boolean
    val fromAddress: String
    val fromName: String
    val verificationBaseUrl: String
    val tokenTtlHours: Int
}
