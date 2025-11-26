package ru.psychologicalTesting.main.infrastructure.controllers.testing.test

import dev.h4kt.ktorDocs.dsl.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.common.parameters.PageParameters
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.testing.test.ExistingTest
import ru.psychologicalTesting.main.infrastructure.repositories.testing.test.TestRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

fun Routing.configureTestRouting() = route("/testing/tests") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    val testRepository by inject<TestRepository>()

    get(::PageParameters) {

        description = "Get all tests"
        tags = listOf("Tests")

        responses {
            HttpStatusCode.OK returns typeInfo<PageResponse<ExistingTest>>()
        }

        handle {

            val result = suspendedTransaction {
                testRepository.findAllPage(
                    offset = parameters.offset,
                    limit = parameters.limit
                )
            }

            call.respond(result)
        }

    }

}
