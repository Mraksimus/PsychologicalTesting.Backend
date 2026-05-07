package ru.psychologicalTesting.main.infrastructure.controllers.admin.test.types.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

open class TestScopeParameters : RouteParameters() {
    val testId by path.uuid {
        name = "test_id"
        description = "Test id to manipulate"
    }
}
