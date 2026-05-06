package ru.psychologicalTesting.main.infrastructure.models

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.lowerCase
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission

object RoleModel : UUIDTable("role") {

    const val NAME_LENGTH = 32

    val name = varchar("name", NAME_LENGTH)
    val color = char("color", 7).nullable()
    val permissions = jsonb<List<Permission>>("permissions", Json.Default)

    init {
        uniqueIndex(
            customIndexName = "role__name_lowercase_unique",
            functions = listOf(name.lowerCase())
        )
    }

}
