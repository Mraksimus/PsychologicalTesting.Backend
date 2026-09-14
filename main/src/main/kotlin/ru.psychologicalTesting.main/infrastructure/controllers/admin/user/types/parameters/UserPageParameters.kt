package ru.psychologicalTesting.main.infrastructure.controllers.admin.user.types.parameters

import ru.psychologicalTesting.main.infrastructure.controllers.common.parameters.PageParameters

class UserPageParameters : PageParameters() {

    val search by query.optionalString {
        name = "search"
        description = "Search users by name, surname or email"
    }

    val roleId by query.optionalUUID {
        name = "role_id"
        description = "Filter users by role id"
    }

}
