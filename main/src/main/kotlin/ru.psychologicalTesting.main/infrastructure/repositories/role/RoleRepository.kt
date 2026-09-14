package ru.psychologicalTesting.main.infrastructure.repositories.role

import ru.psychologicalTesting.main.infrastructure.dto.role.ExistingRole
import ru.psychologicalTesting.main.infrastructure.dto.role.Role
import java.util.*

interface RoleRepository {

    fun create(
        role: Role
    ): ExistingRole

    fun findAll(): List<ExistingRole>

    fun findByUserId(
        userId: UUID
    ): ExistingRole?

    fun existsByName(
        name: String
    ): Boolean

    fun existsByNameExcludingId(
        name: String,
        excludingId: UUID
    ): Boolean

    fun updateById(
        roleId: UUID,
        role: Role
    ): Boolean

    fun deleteById(
        roleId: UUID
    ): Boolean

    fun assignToUser(
        userId: UUID,
        roleId: UUID?
    ): Boolean

}
