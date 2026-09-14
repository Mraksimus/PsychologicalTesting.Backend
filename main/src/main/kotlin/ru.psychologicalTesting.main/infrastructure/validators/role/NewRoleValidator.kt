package ru.psychologicalTesting.main.infrastructure.validators.role

import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.constraints.builders.isMatching
import ru.psychologicalTesting.main.infrastructure.dto.role.NewRole
import ru.psychologicalTesting.main.infrastructure.dto.role.validation.accessors.color
import ru.psychologicalTesting.main.infrastructure.dto.role.validation.accessors.name
import ru.psychologicalTesting.main.infrastructure.models.RoleModel

val newRoleValidator = Validator<NewRole> {

    name {
        isMatching("^[a-zA-Zа-яА-Я0-9 _-]{1,${RoleModel.NAME_LENGTH}}$".toRegex())
    }

    color.isMatching("^#[0-9a-fA-F]{6}$".toRegex())
}
