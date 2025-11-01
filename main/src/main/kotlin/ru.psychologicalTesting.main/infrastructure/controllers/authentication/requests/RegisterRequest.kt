package ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests

import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.isMatching
import dev.nesk.akkurate.constraints.constrain
import dev.nesk.akkurate.constraints.otherwise
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.validation.accessors.email
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.validation.accessors.password
import ru.psychologicalTesting.main.infrastructure.repositories.user.UserRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

@Validate
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
) {
    companion object {

        private val EMAIL_REGEX = """
            ^(?!\.)([a-z0-9._-]{1,250})(?<!\.)@([a-zA-Z0-9.-]{1,64}|[а-яА-Я0-9.-]{1,64}|xn--[a-zA-Z0-9-]{1,61})$
        """.trimIndent().toRegex()

        val validator = Validator.suspendable<UserRepository, RegisterRequest> { userRepository ->

            email {
                isMatching(EMAIL_REGEX)

                constrain {
                    suspendedTransaction {
                        userRepository.findByEmail(it) == null
                    }
                } otherwise {
                    "Email is already taken"
                }
            }

            password {
                isMatching("""[a-zA-Z0-9~\-!@#$%^&*_()\[\]]{6,32}""".toRegex())
            }
        }
    }
}
