package ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests

import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.hasLengthLowerThanOrEqualTo
import dev.nesk.akkurate.constraints.builders.isMatching
import dev.nesk.akkurate.constraints.builders.isNotBlank
import dev.nesk.akkurate.constraints.constrain
import dev.nesk.akkurate.constraints.otherwise
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.validation.accessors.email
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.validation.accessors.name
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.validation.accessors.password
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.validation.accessors.patronymic
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.validation.accessors.surname
import ru.psychologicalTesting.main.infrastructure.repositories.user.UserRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

@Validate
@Serializable
data class RegisterRequest(
    val name: String,
    val surname: String,
    val patronymic: String? = null,
    val email: String,
    val password: String
) {
    companion object {

        private val EMAIL_REGEX = """
            ^(?!\.)[A-Za-z0-9._%+-]{1,64}(?<!\.)@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.)+[A-Za-z]{2,24}$
        """.trimIndent().toRegex()

        val validator = Validator.suspendable<UserRepository, RegisterRequest> { userRepository ->

            name {
                isNotBlank()
                hasLengthLowerThanOrEqualTo(128)
            }

            surname {
                isNotBlank()
                hasLengthLowerThanOrEqualTo(128)
            }

            patronymic {
                hasLengthLowerThanOrEqualTo(128)
            }

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
