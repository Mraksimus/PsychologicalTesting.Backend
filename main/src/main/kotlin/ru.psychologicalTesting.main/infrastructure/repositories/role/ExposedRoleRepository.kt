package ru.psychologicalTesting.main.infrastructure.repositories.role

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.role.ExistingRole
import ru.psychologicalTesting.main.infrastructure.dto.role.Role
import ru.psychologicalTesting.main.infrastructure.models.RoleModel
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import java.util.*

@Single
class ExposedRoleRepository : RoleRepository {

    override fun create(
        role: Role
    ): ExistingRole {

        val insertedRow = RoleModel.insert {
            it[name] = role.name
            it[color] = role.color
            it[permissions] = role.permissions
        }

        return ExistingRole(
            id = insertedRow[RoleModel.id].value,
            name = role.name,
            color = role.color,
            permissions = role.permissions
        )
    }

    override fun findAll(): List<ExistingRole> {
        return RoleModel
            .selectAll()
            .map { it.toExistingRole() }
    }

    override fun findByUserId(
        userId: UUID
    ): ExistingRole? {
        return UserModel
            .innerJoin(RoleModel)
            .select(RoleModel.columns)
            .where { UserModel.id eq userId }
            .firstOrNull()
            ?.toExistingRole()
    }

    override fun existsByName(
        name: String
    ): Boolean {
        return RoleModel
            .selectAll()
            .where { RoleModel.name.lowerCase() eq name.lowercase() }
            .count() > 0
    }

    override fun existsByNameExcludingId(
        name: String,
        excludingId: UUID
    ): Boolean {
        return RoleModel
            .selectAll()
            .where {
                (RoleModel.name.lowerCase() eq name.lowercase()) and
                    (RoleModel.id neq excludingId)
            }
            .count() > 0
    }

    override fun updateById(
        roleId: UUID,
        role: Role
    ): Boolean {

        val affectedRows = RoleModel.updateById(roleId) {
            it[name] = role.name
            it[color] = role.color
            it[permissions] = role.permissions
        }

        return affectedRows > 0
    }

    override fun deleteById(
        roleId: UUID
    ): Boolean {
        val affectedRows = RoleModel.deleteById(roleId)
        return affectedRows > 0
    }

    override fun assignToUser(
        userId: UUID,
        roleId: UUID?
    ): Boolean {
        val affectedRows = UserModel.updateById(userId) {
            it[this.roleId] = roleId
        }
        return affectedRows > 0
    }

    private fun ResultRow.toExistingRole() = ExistingRole(
        id = this[RoleModel.id].value,
        name = this[RoleModel.name],
        color = this[RoleModel.color],
        permissions = this[RoleModel.permissions]
    )

}
