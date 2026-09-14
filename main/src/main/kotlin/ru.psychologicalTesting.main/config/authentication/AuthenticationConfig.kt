package ru.psychologicalTesting.main.config.authentication

import ru.psychologicalTesting.common.compat.DurationString
import ru.psychologicalTesting.common.compat.SerialRegex

interface AuthenticationConfig {

    val issuer: String
    val audience: String
    val tokenTtl: DurationString

    val publicKeyPath: String
    val privateKeyPath: String

    val emailRegex: SerialRegex
    val passwordRegex: SerialRegex

}
