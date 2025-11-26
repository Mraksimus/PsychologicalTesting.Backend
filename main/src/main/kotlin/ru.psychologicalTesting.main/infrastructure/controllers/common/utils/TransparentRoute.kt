package ru.psychologicalTesting.main.infrastructure.controllers.common.utils

import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.RouteSelectorEvaluation
import io.ktor.server.routing.RoutingResolveContext

private class TransparentRouteSelector : RouteSelector() {

    override suspend fun evaluate(
        context: RoutingResolveContext,
        segmentIndex: Int
    ) = RouteSelectorEvaluation.Transparent

    override fun toString() = ""

}

fun Route.transparentRoute(
    builder: Route.() -> Unit
): Route {
    return createChild(TransparentRouteSelector()).apply(builder)
}
