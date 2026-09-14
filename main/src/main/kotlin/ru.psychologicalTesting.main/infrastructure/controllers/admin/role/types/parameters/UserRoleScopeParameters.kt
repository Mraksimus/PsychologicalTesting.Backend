package ru.psychologicalTesting.main.infrastructure.controllers.admin.role.types.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

class UserRoleScopeParameters : RouteParameters() {
    val userId by path.uuid {
        name = "user_id"
        description = "User id to assign a role to"
    }
}
