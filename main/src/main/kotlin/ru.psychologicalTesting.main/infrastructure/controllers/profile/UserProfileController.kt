package ru.psychologicalTesting.main.infrastructure.controllers.profile

import dev.h4kt.ktorDocs.dsl.delete
import dev.h4kt.ktorDocs.dsl.get
import dev.h4kt.ktorDocs.dsl.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.BadRequestResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.controllers.profile.requests.ChangeUserFullNameRequest
import ru.psychologicalTesting.main.infrastructure.dto.user.UserProfile
import ru.psychologicalTesting.main.infrastructure.services.user.UserService
import ru.psychologicalTesting.main.infrastructure.services.user.results.ChangeUserFullNameResult
import ru.psychologicalTesting.main.infrastructure.services.user.results.GetUserProfileResult
import ru.psychologicalTesting.main.plugins.authentication.UserPrincipal
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "User profile"

fun Routing.conffigureUserProfileRouting() = route("/user/profile") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    val userService by inject<UserService>()

    get {

        description = "Get user profile information"
        tags = listOf(SWAGGER_TAG)

        responses {
            HttpStatusCode.OK returns typeInfo<UserProfile>()
            HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
            HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
        }

        handle {

            val (userId) = call.principal<UserPrincipal>()!!

            val result = suspendedTransaction {
                userService.getUserProfileById(userId)
            }

            when (result) {
                is GetUserProfileResult.UserNotFound ->
                    call.respondNotFound("User(id=$userId) does not exist")
                is GetUserProfileResult.Success ->
                    call.respond(HttpStatusCode.OK, result.userProfile)
            }
        }

    }

    put {

        description = "Update user full name"
        tags = listOf(SWAGGER_TAG)

        requestBody = typeInfo<ChangeUserFullNameRequest>()

        responses {
            HttpStatusCode.OK returns typeInfo<String>()
            HttpStatusCode.NotFound returns typeInfo<NotFoundResponse>()
            HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
        }

        handle {

            val (userId) = call.principal<UserPrincipal>()!!

            val (
                name,
                surname,
                patronymic
            ) = call.receive<ChangeUserFullNameRequest>()

            val result = suspendedTransaction {
                userService.changeUserFullName(
                    userId = userId,
                    name = name,
                    surname = surname,
                    patronymic = patronymic
                )
            }

            when (result) {
                is ChangeUserFullNameResult.UserNotFound ->
                    call.respondNotFound("User(id=$userId) does not exist")
                is ChangeUserFullNameResult.UserFullNameWasNotUpdated ->
                    call.respond(HttpStatusCode.InternalServerError, "User(id=$userId) was not updated")
                is ChangeUserFullNameResult.Success ->
                    call.respond(HttpStatusCode.OK, "User(id=$userId) is updated")
            }
        }

    }

    delete {

        description = "Delete user"
        tags = listOf(SWAGGER_TAG)

        responses {
            HttpStatusCode.OK returns typeInfo<String>()
            HttpStatusCode.BadRequest returns typeInfo<BadRequestResponse>()
        }

        handle {

            val (userId) = call.principal<UserPrincipal>()!!

            val result = suspendedTransaction {
                userService.delete(userId)
            }

            if (result) {
                call.respond(HttpStatusCode.OK, "User(id=$userId) was deleted")
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

    }

}
