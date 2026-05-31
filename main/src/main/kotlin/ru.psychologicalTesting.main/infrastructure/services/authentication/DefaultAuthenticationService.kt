package ru.psychologicalTesting.main.infrastructure.services.authentication

import com.appstractive.jwt.jwt
import com.appstractive.jwt.sign
import com.appstractive.jwt.signatures.es256
import com.appstractive.jwt.subject
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import io.ktor.server.auth.jwt.JWTCredential
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import ru.psychologicalTesting.main.config.authentication.AuthenticationConfig
import ru.psychologicalTesting.main.infrastructure.dto.authentication.ExistingSession
import ru.psychologicalTesting.main.infrastructure.dto.authentication.NewSession
import ru.psychologicalTesting.main.infrastructure.repositories.authentication.SessionRepository
import ru.psychologicalTesting.main.infrastructure.repositories.user.UserRepository
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.CredentialValidationResult
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.LoginResult
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.RegistrationResult
import ru.psychologicalTesting.main.infrastructure.services.user.UserService
import ru.psychologicalTesting.main.utils.now
import ru.psychologicalTesting.main.utils.nowUTC
import java.io.File
import java.util.UUID
import kotlin.time.ExperimentalTime

@Single(createdAtStart = true)
class DefaultAuthenticationService(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val config: AuthenticationConfig
) : AuthenticationService {

    private val logger = LoggerFactory.getLogger(DefaultAuthenticationService::class.java)

    override val curve = EC.Curve.P256
    override val publicKey: ECDSA.PublicKey
    private val privateKey: ECDSA.PrivateKey

    init {
        val publicKeyFile = File(config.publicKeyPath)
        val privateKeyFile = File(config.privateKeyPath)

        val ecdsa = CryptographyProvider.Default.get(ECDSA)

        if (publicKeyFile.exists() && privateKeyFile.exists()) {
            logger.info("Decoding JWT ECDSA key pair")

            publicKey = decodePublicKey(ecdsa, publicKeyFile)
            privateKey = decodePrivateKey(ecdsa, privateKeyFile)
        } else {
            logger.info("Generating JWT ECDSA key pair")

            val keys = ecdsa
                .keyPairGenerator(curve)
                .generateKeyBlocking()

            publicKey = keys.publicKey
            privateKey = keys.privateKey

            savePublicKey(publicKey, publicKeyFile)
            savePrivateKey(privateKey, privateKeyFile)
        }
    }

    override fun register(
        name: String,
        surname: String,
        patronymic: String?,
        email: String,
        password: String,
        userAgent: String,
        ipAddress: String
    ): RegistrationResult {

        val user = userService.create(
            name = name,
            surname = surname,
            patronymic = patronymic,
            email = email,
            password = password
        )

        val (session, token) = createSession(
            userId = user.id,
            userAgent = userAgent,
            ipAddress = ipAddress
        )

        return RegistrationResult.Success(
            session = session,
            token = token
        )
    }

    override fun login(
        email: String,
        password: String,
        userAgent: String,
        ipAddress: String
    ): LoginResult {

        val user = userRepository.findByEmail(email)
            ?: return LoginResult.InvalidCredentials

        val isPasswordValid = try {
            BCrypt.checkpw(password, user.password)
        } catch (_: Exception) {
            false
        }

        if (!isPasswordValid) {
            return LoginResult.InvalidCredentials
        }

        userRepository.update(
            user.copy(
                lastLoginAt = LocalDateTime.now()
            )
        )

        val (session, token) = createSession(
            userId = user.id,
            userAgent = userAgent,
            ipAddress = ipAddress
        )

        return LoginResult.Success(
            session = session,
            token = token
        )
    }

    @OptIn(ExperimentalTime::class)
    override fun createSession(
        userId: UUID,
        userAgent: String,
        ipAddress: String
    ): Pair<ExistingSession, String> {

        val now = LocalDateTime.nowUTC()

        val session = sessionRepository.create(
            session = NewSession(
                userId = userId,
                userAgent = userAgent.take(SESSION_USER_AGENT_LIMIT),
                ipAddress = ipAddress,
                lastLoginAt = now
            )
        )

        val unsignedToken = jwt {
            claims {
                issuer = config.issuer
                audience = config.audience

                subject = session.id.toString()

                val nowInstant = kotlin.time.Clock.System.now()
                expires(nowInstant + config.tokenTtl)
                issuedAt(nowInstant)
            }
        }

        val signedToken = runBlocking {
            val key = privateKey.encodeToByteArray(EC.PrivateKey.Format.PEM)
            unsignedToken.sign {
                es256 {
                    pem(key, curve)
                }
            }
        }

        return session to signedToken.toString()
    }

    override fun validateCredential(
        credential: JWTCredential
    ): CredentialValidationResult {
        val sessionId = credential.claims.subject
            ?.let(::tryParseUuid)
            ?: return CredentialValidationResult.Invalid

        val session = transaction {
            sessionRepository.findById(sessionId)
        } ?: return CredentialValidationResult.Invalid

        return CredentialValidationResult.Success(session)
    }

    private fun tryParseUuid(value: String): UUID? = try {
        UUID.fromString(value)
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun decodePublicKey(ecdsa: ECDSA, file: File): ECDSA.PublicKey {
        val bytes = try {
            file.readBytes()
        } catch (ex: Exception) {
            error("Unable to read public key file ${file.absolutePath}: ${ex.message}")
        }
        return try {
            ecdsa.publicKeyDecoder(curve)
                .decodeFromByteArrayBlocking(EC.PublicKey.Format.PEM, bytes)
        } catch (ex: Exception) {
            error("Unable to decode public key: ${ex.message}")
        }
    }

    private fun decodePrivateKey(ecdsa: ECDSA, file: File): ECDSA.PrivateKey {
        val bytes = try {
            file.readBytes()
        } catch (ex: Exception) {
            error("Unable to read private key file ${file.absolutePath}: ${ex.message}")
        }
        return try {
            ecdsa.privateKeyDecoder(curve)
                .decodeFromByteArrayBlocking(EC.PrivateKey.Format.PEM, bytes)
        } catch (ex: Exception) {
            error("Unable to decode private key: ${ex.message}")
        }
    }

    private fun savePublicKey(key: ECDSA.PublicKey, file: File) {
        try {
            file.parentFile?.mkdirs()
            file.writeBytes(key.encodeToByteArrayBlocking(EC.PublicKey.Format.PEM))
        } catch (ex: Exception) {
            error("Unable to save public key: ${ex.message}")
        }
    }

    private fun savePrivateKey(key: ECDSA.PrivateKey, file: File) {
        try {
            file.parentFile?.mkdirs()
            file.writeBytes(key.encodeToByteArrayBlocking(EC.PrivateKey.Format.PEM))
        } catch (ex: Exception) {
            error("Unable to save private key: ${ex.message}")
        }
    }

    private companion object {
        const val SESSION_USER_AGENT_LIMIT = 512
    }

}
