package ru.psychologicalTesting.main.config.authentication

import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.DurationString
import ru.psychologicalTesting.common.compat.SerialRegex

@Serializable
data class SerialAuthenticationConfig(
    override val issuer: String,
    override val audience: String,
    override val tokenTtl: DurationString,
    override val publicKeyPath: String,
    override val privateKeyPath: String,
    override val emailRegex: SerialRegex,
    override val passwordRegex: SerialRegex,
) : AuthenticationConfig
