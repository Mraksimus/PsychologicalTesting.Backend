package ru.psychologicalTesting.main.infrastructure.controllers.admin.user.types.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

class UserScopeParameters : RouteParameters() {
    val userId by path.uuid {
        name = "user_id"
        description = "User id"
    }
}
