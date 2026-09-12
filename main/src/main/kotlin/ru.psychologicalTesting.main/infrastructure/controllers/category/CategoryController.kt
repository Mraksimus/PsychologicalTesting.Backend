package ru.psychologicalTesting.main.infrastructure.controllers.category

import dev.h4kt.ktorDocs.dsl.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.common.category.ExistingCategory
import ru.psychologicalTesting.main.infrastructure.repositories.category.CategoryRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

fun Routing.configureCategoryRouting() = route("/categories") {

    val categoryRepository by inject<CategoryRepository>()

    get {

        description = "Get all categories"
        tags = listOf("Categories")

        responses {
            HttpStatusCode.OK returns typeInfo<List<ExistingCategory>>()
        }

        handle {
            val result = suspendedTransaction {
                categoryRepository.findAll()
            }
            call.respond(result)
        }
    }
}
