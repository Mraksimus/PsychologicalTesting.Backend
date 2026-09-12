package ru.psychologicalTesting.main.infrastructure.controllers.admin.category

import dev.h4kt.ktorDocs.dsl.delete
import dev.h4kt.ktorDocs.dsl.get
import dev.h4kt.ktorDocs.dsl.patch
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
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.common.category.ExistingCategory
import ru.psychologicalTesting.common.category.NewCategory
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.requirePermissions
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ForbiddenResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.NotFoundResponse
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.respondNotFound
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.repositories.category.CategoryRepository
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Categories (admin)"

open class CategoryScopeParameters : RouteParameters() {
    val categoryId by path.uuid {
        name = "category_id"
        description = "Category id to manipulate"
    }
}

fun Routing.configureAdminCategoryRouting() = route("/admin/categories") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    val categoryRepository by inject<CategoryRepository>()

    requirePermissions(Permission.CATEGORIES_VIEW) {

        get {

            description = "List all categories"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<List<ExistingCategory>>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {
                val result = suspendedTransaction {
                    categoryRepository.findAll()
                }
                call.respond(result)
            }
        }
    }

    requirePermissions(Permission.CATEGORIES_EDIT) {

        post("new") {

            description = "Create a new category"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewCategory>()

            responses {
                HttpStatusCode.Created returns typeInfo<ExistingCategory>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {
                val body = call.receive<NewCategory>()
                val category = suspendedTransaction {
                    categoryRepository.create(body)
                }
                call.respond(HttpStatusCode.Created, category)
            }
        }

        patch("{category_id}", ::CategoryScopeParameters) {

            description = "Update a category"
            tags = listOf(SWAGGER_TAG)

            requestBody = typeInfo<NewCategory>()

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Category does not exist"
                }
            }

            handle {
                val body = call.receive<NewCategory>()
                val isUpdated = suspendedTransaction {
                    categoryRepository.update(id = parameters.categoryId, dto = body)
                }

                if (isUpdated) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Category(id=${parameters.categoryId}) does not exist")
                }
            }
        }

        delete("{category_id}", ::CategoryScopeParameters) {

            description = "Delete a category"
            tags = listOf(SWAGGER_TAG)

            responses {
                noContent()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()

                HttpStatusCode.NotFound returns {
                    body = typeInfo<NotFoundResponse>()
                    description = "Category does not exist"
                }
            }

            handle {
                val isDeleted = suspendedTransaction {
                    categoryRepository.delete(parameters.categoryId)
                }

                if (isDeleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respondNotFound("Category(id=${parameters.categoryId}) does not exist")
                }
            }
        }
    }
}
