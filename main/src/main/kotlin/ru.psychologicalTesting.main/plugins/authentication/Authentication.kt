package ru.psychologicalTesting.main.plugins.authentication

import com.appstractive.jwt.signatures.es256
import dev.whyoleg.cryptography.algorithms.EC
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.jwt
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.config.authentication.AuthenticationConfig
import ru.psychologicalTesting.main.infrastructure.repositories.role.RoleRepository
import ru.psychologicalTesting.main.infrastructure.services.authentication.AuthenticationService
import ru.psychologicalTesting.main.infrastructure.services.authentication.results.CredentialValidationResult

fun Application.configureAuthentication() {

    val config by inject<AuthenticationConfig>()
    val authenticationService by inject<AuthenticationService>()
    val roleRepository by inject<RoleRepository>()

    authentication {
        jwt("user") {
            val curve = authenticationService.curve
            val encodedKey = authenticationService.publicKey
                .encodeToByteArrayBlocking(EC.PublicKey.Format.PEM)

            verifier(
                issuer = config.issuer,
                audience = config.audience
            ) {
                es256 {
                    pem(encodedKey, curve)
                }
            }

            validate { credential ->
                val result = authenticationService.validateCredential(credential)
                if (result !is CredentialValidationResult.Success) {
                    return@validate null
                }

                val (session) = result

                val role = transaction {
                    roleRepository.findByUserId(session.userId)
                }

                UserPrincipal(
                    id = session.userId,
                    role = role,
                    session = session
                )
            }
        }
    }

}
