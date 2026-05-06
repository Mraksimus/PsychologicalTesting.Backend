package ru.psychologicalTesting.main.infrastructure.controllers.admin.role.types.parameters

import dev.h4kt.ktorDocs.types.parameters.RouteParameters

open class RoleScopeParameters : RouteParameters() {
    val roleId by path.uuid {
        name = "role_id"
        description = "Role id to manipulate"
    }
}
