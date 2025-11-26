package ru.psychologicalTesting.main.infrastructure.controllers.testing.session.types.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

class CreateSessionParameters : RouteParameters() {
    val testId by path.uuid {
        name = "test_id"
        description = "Test id to create"
    }
}
