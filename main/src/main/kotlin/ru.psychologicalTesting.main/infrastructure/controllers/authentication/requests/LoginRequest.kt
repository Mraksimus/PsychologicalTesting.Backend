package ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests

import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.isNotBlank
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.validation.accessors.email
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.requests.validation.accessors.password

@Validate
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
) {
    companion object {
        val validator = Validator<LoginRequest> {
            email.isNotBlank()
            password.isNotBlank()
        }
    }
}
