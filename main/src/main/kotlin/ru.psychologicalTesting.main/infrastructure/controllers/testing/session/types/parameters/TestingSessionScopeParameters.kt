package ru.psychologicalTesting.main.infrastructure.controllers.testing.session.types.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

class TestingSessionScopeParameters : RouteParameters() {
    val sessionId by path.uuid {
        name = "session_id"
        description = "Session id to manipulate"
    }
}
