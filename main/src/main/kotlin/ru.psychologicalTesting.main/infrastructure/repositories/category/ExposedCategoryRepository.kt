package ru.psychologicalTesting.main.infrastructure.repositories.category

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.category.ExistingCategory
import ru.psychologicalTesting.common.category.NewCategory
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.models.category.CategoryModel
import ru.psychologicalTesting.main.utils.now
import java.util.UUID

@Single
class ExposedCategoryRepository : CategoryRepository {

    override fun create(dto: NewCategory): ExistingCategory {
        val positionExpression = CategoryModel.position.max()
        val position = CategoryModel
            .select(positionExpression)
            .firstOrNull()
            ?.let { it[positionExpression]?.inc() }
            ?: 0

        val row = CategoryModel.insert {
            it[name] = dto.name
            it[color] = dto.color
            it[icon] = dto.icon
            it[this.position] = position
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }

        return ExistingCategory(
            id = row[CategoryModel.id].value,
            name = row[CategoryModel.name],
            color = row[CategoryModel.color],
            icon = row[CategoryModel.icon],
            position = row[CategoryModel.position],
            createdAt = row[CategoryModel.createdAt],
            updatedAt = row[CategoryModel.updatedAt],
        )
    }

    override fun findOneById(id: UUID): ExistingCategory? = CategoryModel
        .selectAll()
        .where(CategoryModel.id eq id)
        .firstOrNull()
        ?.toExistingCategory()

    override fun findAll(): List<ExistingCategory> = CategoryModel
        .selectAll()
        .map { it.toExistingCategory() }
        .sortedBy { it.position }

    override fun update(id: UUID, dto: NewCategory): Boolean {
        val affected = CategoryModel.updateById(id) {
            it[name] = dto.name
            it[color] = dto.color
            it[icon] = dto.icon
            it[updatedAt] = LocalDateTime.now()
        }
        return affected > 0
    }

    override fun delete(id: UUID): Boolean = CategoryModel.deleteById(id) > 0

    private fun ResultRow.toExistingCategory() = ExistingCategory(
        id = this[CategoryModel.id].value,
        name = this[CategoryModel.name],
        color = this[CategoryModel.color],
        icon = this[CategoryModel.icon],
        position = this[CategoryModel.position],
        createdAt = this[CategoryModel.createdAt],
        updatedAt = this[CategoryModel.updatedAt],
    )
}
