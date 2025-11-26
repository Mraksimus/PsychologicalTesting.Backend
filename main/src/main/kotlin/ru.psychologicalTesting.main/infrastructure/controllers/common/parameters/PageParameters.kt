package ru.psychologicalTesting.main.infrastructure.controllers.common.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

open class PageParameters : RouteParameters() {

    val offset by query.long {
        name = "offset"
        description = "Offset of the first item to return"
    }

    val limit by query.int {
        name = "limit"
        description = "Maximum number of items to return"
    }

}
