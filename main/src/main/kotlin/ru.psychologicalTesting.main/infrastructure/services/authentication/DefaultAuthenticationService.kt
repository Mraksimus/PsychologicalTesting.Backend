package ru.psychologicalTesting.main.infrastructure.services.authentication

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single
import org.mindrot.jbcrypt.BCrypt
import ru.psychologicalTesting.main.infrastructure.dto.Token
import ru.psychologicalTesting.main.infrastructure.repositories.authentication.TokenRepository
import ru.psychologicalTesting.main.infrastructure.repositories.user.UserRepository
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.LoginResult
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.RegistrationResult
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.TokenValidationResult
import ru.psychologicalTesting.main.infrastructure.services.user.UserService
import ru.psychologicalTesting.main.utils.nowUTC
import ru.psychologicalTesting.main.utils.plus
import java.util.*
import kotlin.time.Duration.Companion.days

@Single
class DefaultAuthenticationService(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository
) : AuthenticationService {

    private val tokenCharacters = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    override fun register(
        name: String,
        surname: String,
        patronymic: String?,
        email: String,
        password: String
    ): RegistrationResult {

        val user = userService.create(
            name = name,
            surname = surname,
            patronymic = patronymic,
            email = email,
            password = password
        )

        return RegistrationResult.Success(
            token = generateToken(user.id)
        )
    }

    override fun login(
        email: String,
        password: String
    ): LoginResult {

        val user = userRepository.findByEmail(email)
            ?: return LoginResult.InvalidCredentials

        val passwordHash = user.password

        val isPasswordValid = try {
            BCrypt.checkpw(password, passwordHash)
        } catch (_: Exception) {
            false
        }

        if (!isPasswordValid) {
            return LoginResult.InvalidCredentials
        }

        tokenRepository.clearExpired()

        return LoginResult.Success(
            token = generateToken(user.id)
        )
    }

    override fun validateToken(value: String): TokenValidationResult = transaction {

        val token = tokenRepository.findOneByValue(value)
            ?: return@transaction TokenValidationResult.Invalid

        val currentDateTime = LocalDateTime.nowUTC()

        tokenRepository.update(
            userId = token.userId,
            dto = token.copy(
                value = token.value,
                expiresAt = currentDateTime + 14.days
            )
        )

        return@transaction TokenValidationResult.Success(
            userId = token.userId
        )
    }

    private fun generateToken(userId: UUID): Token {

        val currentDateTime = LocalDateTime.nowUTC()

        return tokenRepository.create(
            Token(
                userId = userId,
                value = Array(64) { tokenCharacters.random() }.joinToString(""),
                createdAt = currentDateTime,
                expiresAt = currentDateTime + 14.days
            )
        )
    }

}
