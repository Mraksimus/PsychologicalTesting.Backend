package ru.psychologicalTesting.main.infrastructure.repositories.category

import ru.psychologicalTesting.common.category.ExistingCategory
import ru.psychologicalTesting.common.category.NewCategory
import java.util.UUID

interface CategoryRepository {
    fun create(dto: NewCategory): ExistingCategory
    fun findOneById(id: UUID): ExistingCategory?
    fun findAll(): List<ExistingCategory>
    fun update(id: UUID, dto: NewCategory): Boolean
    fun delete(id: UUID): Boolean
}
