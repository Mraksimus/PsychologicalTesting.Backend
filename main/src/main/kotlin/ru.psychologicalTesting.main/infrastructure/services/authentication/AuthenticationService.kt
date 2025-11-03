package ru.psychologicalTesting.main.infrastructure.services.authentication

import ru.psychologicalTesting.main.infrastructure.services.authentication.results.LoginResult
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.RegistrationResult
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.TokenValidationResult

interface AuthenticationService {

    fun register(
        name: String,
        surname: String,
        patronymic: String?,
        email: String,
        password: String
    ): RegistrationResult

    fun login(
        email: String,
        password: String
    ): LoginResult

    fun validateToken(
        value: String
    ): TokenValidationResult

}
