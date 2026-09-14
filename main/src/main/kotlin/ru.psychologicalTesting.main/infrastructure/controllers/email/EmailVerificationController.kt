package ru.psychologicalTesting.main.infrastructure.controllers.email

import dev.h4kt.ktorDocs.dsl.post
import dev.h4kt.ktorDocs.types.parameters.RouteParameters
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.BadRequestResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondBadRequest
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.services.email.EmailVerificationService
import ru.psychologicalTesting.main.plugins.authentication.UserPrincipal
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Email verification"

@Serializable
data class ConfirmEmailRequest(val token: String)

@Serializable
data class ResendEmailResponse(val emailSent: Boolean)

open class VerifyScopeParameters : RouteParameters()

fun Routing.configureEmailVerificationRouting() = route("/auth/verify") {

    val verificationService by inject<EmailVerificationService>()

    post("confirm") {

        description = "Confirm email using a verification token"
        tags = listOf(SWAGGER_TAG)

        requestBody = typeInfo<ConfirmEmailRequest>()

        responses {
            noContent()
            HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
            HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
        }

        handle {
            val body = call.receive<ConfirmEmailRequest>()
            val token = body.token.trim()

            if (token.isEmpty()) {
                call.respondBadRequest("Пустой токен")
                return@handle
            }

            val result = suspendedTransaction {
                verificationService.confirm(token)
            }

            when (result) {
                EmailVerificationService.ConfirmResult.Success ->
                    call.respond(HttpStatusCode.NoContent)
                EmailVerificationService.ConfirmResult.TokenNotFound ->
                    call.respondNotFound("Токен не найден")
                EmailVerificationService.ConfirmResult.TokenExpired ->
                    call.respondBadRequest("Срок действия токена истёк")
                EmailVerificationService.ConfirmResult.TokenAlreadyUsed ->
                    call.respondBadRequest("Токен уже использован")
            }
        }
    }

    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    val verificationService by inject<EmailVerificationService>()

    post("resend") {

        description = "Send a fresh verification link to the current user"
        tags = listOf(SWAGGER_TAG)

        responses {
            HttpStatusCode.OK returns typeInfo<ResendEmailResponse>()
            HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
            HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
        }

        handle {
            val principal = call.principal<UserPrincipal>()!!

            val result = verificationService.issueForUser(principal.id)

            when (result) {
                is EmailVerificationService.IssueResult.Success ->
                    call.respond(ResendEmailResponse(emailSent = result.emailSent))
                is EmailVerificationService.IssueResult.AlreadyVerified ->
                    call.respondBadRequest("Email уже подтверждён")
                is EmailVerificationService.IssueResult.UserNotFound ->
                    call.respondNotFound("Пользователь не найден")
                is EmailVerificationService.IssueResult.Failed ->
                    call.respond(HttpStatusCode.ServiceUnavailable, "Не удалось отправить письмо")
            }
        }
    }
}
