package ru.psychologicalTesting.main.infrastructure.controllers

import dev.h4kt.ktorDocs.dsl.get
import dev.h4kt.ktorDocs.dsl.post
import dev.h4kt.ktorDocs.types.parameters.RouteParameters
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.Test
import ru.psychologicalTesting.main.infrastructure.repositories.test.TestRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction
import org.koin.ktor.ext.inject

fun Routing.configureTestRouting() = route("/test") {

    configurePublicRoutes()

    authenticate("template") {
        configureAuthenticatedRoutes()
    }

}

private fun Route.configurePublicRoutes() {

    val testRepository by inject<TestRepository>()

    class GetParams : RouteParameters() {

        val offset by query.long {
            name = "offset"
        }

        val limit by query.int {
            name = "limit"
        }

    }

    get(::GetParams) {

        description = "Get list of all test objects"

        responses {
            HttpStatusCode.OK returns typeInfo<PageResponse<Test>>()
        }

        handle {

            val result = suspendedTransaction {
                testRepository.findAll(
                    offset = parameters.offset,
                    limit = parameters.limit
                )
            }

            call.respond(result)
        }

    }

}

private fun Route.configureAuthenticatedRoutes() {

    val testRepository by inject<TestRepository>()

    post("new") {

        description = "Create new test object"
        requestBody = typeInfo<Test>()

        handle {

            val body = call.receive<Test>()

            val result = suspendedTransaction {
                testRepository.create(body)
            }

            call.respond(result)
        }

    }

}
