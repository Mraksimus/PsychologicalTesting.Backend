package ru.psychologicalTesting.main.infrastructure.services.authentication

import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import io.ktor.server.auth.jwt.JWTCredential
import ru.psychologicalTesting.main.infrastructure.dto.authentication.ExistingSession
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.CredentialValidationResult
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.LoginResult
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.RegistrationResult
import java.util.UUID

interface AuthenticationService {

    val curve: EC.Curve
    val publicKey: ECDSA.PublicKey

    fun register(
        name: String,
        surname: String,
        patronymic: String?,
        email: String,
        password: String,
        userAgent: String,
        ipAddress: String
    ): RegistrationResult

    fun login(
        email: String,
        password: String,
        userAgent: String,
        ipAddress: String
    ): LoginResult

    fun createSession(
        userId: UUID,
        userAgent: String,
        ipAddress: String
    ): Pair<ExistingSession, String>

    fun validateCredential(
        credential: JWTCredential
    ): CredentialValidationResult

}
