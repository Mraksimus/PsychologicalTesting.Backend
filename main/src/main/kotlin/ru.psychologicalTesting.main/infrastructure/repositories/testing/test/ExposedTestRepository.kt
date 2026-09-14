package ru.psychologicalTesting.main.infrastructure.repositories.testing.test

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.category.ExistingCategory
import ru.psychologicalTesting.common.testing.test.ExistingTest
import ru.psychologicalTesting.common.testing.test.NewTest
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.models.category.CategoryModel
import ru.psychologicalTesting.main.infrastructure.models.testing.QuestionModel
import ru.psychologicalTesting.main.infrastructure.models.testing.TestModel
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class ExposedTestRepository : TestRepository {

    override fun create(
        dto: NewTest
    ): ExistingTest {

        val positionExpression = TestModel.position.max()

        val position = TestModel
            .select(positionExpression)
            .firstOrNull()
            ?.let {
                it[positionExpression]?.inc()
            }
            ?: 0

        val insertedRow = TestModel.insert {
            it[name] = dto.name
            it[description] = dto.description
            it[transcript] = dto.transcript
            it[durationMins] = dto.durationMins
            it[isActive] = dto.isActive
            it[categoryId] = dto.categoryId
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
            it[this.position] = position
        }

        val id = insertedRow[TestModel.id].value

        return ExistingTest(
            id = id,
            name = insertedRow[TestModel.name],
            description = insertedRow[TestModel.description],
            transcript = insertedRow[TestModel.transcript],
            durationMins = insertedRow[TestModel.durationMins],
            isActive = insertedRow[TestModel.isActive],
            categoryId = insertedRow[TestModel.categoryId]?.value,
            category = insertedRow[TestModel.categoryId]?.value?.let(::loadCategory),
            questionsCount = 0,
            createdAt = insertedRow[TestModel.createdAt],
            updatedAt = insertedRow[TestModel.updatedAt],
            position = insertedRow[TestModel.position]
        )
    }

    override fun findOneById(
        id: UUID
    ): ExistingTest? {
        val row = TestModel
            .selectAll()
            .where(TestModel.id eq id)
            .firstOrNull()
            ?: return null

        val test = row.toExistingTest()
        val category = test.categoryId?.let(::loadCategory)
        val count = countQuestions(listOf(test.id))[test.id] ?: 0
        return test.copy(category = category, questionsCount = count)
    }

    override fun findAllPage(
        offset: Long,
        limit: Int,
        activeOnly: Boolean
    ): PageResponse<ExistingTest> {

        fun query(): Query = if (activeOnly) {
            TestModel.selectAll().where { TestModel.isActive eq true }
        } else {
            TestModel.selectAll()
        }

        val totalCount = query().count()

        val tests = query()
            .offset(offset)
            .limit(limit)
            .map { it.toExistingTest() }
            .sortedBy { it.position }

        val categoryIds = tests.mapNotNull { it.categoryId }.toSet()
        val categoriesById = if (categoryIds.isEmpty()) emptyMap() else loadCategories(categoryIds)
        val counts = countQuestions(tests.map { it.id })

        val enriched = tests.map {
            it.copy(
                category = it.categoryId?.let(categoriesById::get),
                questionsCount = counts[it.id] ?: 0
            )
        }

        return PageResponse(
            total = totalCount,
            offset = offset,
            limit = limit,
            items = enriched
        )
    }

    override fun update(
        id: UUID,
        dto: NewTest
    ): Boolean {

        val affectedRows = TestModel.updateById(id) {
            it[name] = dto.name
            it[description] = dto.description
            it[transcript] = dto.transcript
            it[durationMins] = dto.durationMins
            it[isActive] = dto.isActive
            it[categoryId] = dto.categoryId
            it[updatedAt] = LocalDateTime.now()
        }

        return affectedRows > 0
    }

    override fun updatePositionById(
        id: UUID,
        position: Int
    ): Boolean {

        val test = TestModel
            .selectAll()
            .where(TestModel.id eq id)
            .map { it.toExistingTest() }
            .firstOrNull()
            ?: return false

        val oldPosition = test.position
        if (oldPosition == position) {
            return true
        }

        if (position < oldPosition) {

            TestModel.update(
                where = {
                    (TestModel.position greaterEq position)
                        .and(TestModel.position lessEq oldPosition)
                }
            ) {
                it[this.position] = TestModel.position + 1
            }

        } else {

            TestModel.update(
                where = {
                    (TestModel.position greaterEq oldPosition)
                        .and(TestModel.position lessEq position)
                }
            ) {
                it[this.position] = TestModel.position - 1
            }

        }

        TestModel.updateById(id) {
            it[this.position] = position
        }

        return true
    }

    override fun delete(id: UUID): Boolean {
        return TestModel.deleteById(id) > 0
    }

    private fun ResultRow.toExistingTest() = ExistingTest(
        id = this[TestModel.id].value,
        name = this[TestModel.name],
        description = this[TestModel.description],
        transcript = this[TestModel.transcript],
        durationMins = this[TestModel.durationMins],
        isActive = this[TestModel.isActive],
        categoryId = this[TestModel.categoryId]?.value,
        category = null,
        questionsCount = 0,
        createdAt = this[TestModel.createdAt],
        updatedAt = this[TestModel.updatedAt],
        position = this[TestModel.position]
    )

    private fun loadCategory(id: UUID): ExistingCategory? = CategoryModel
        .selectAll()
        .where(CategoryModel.id eq id)
        .firstOrNull()
        ?.toExistingCategory()

    private fun loadCategories(ids: Set<UUID>): Map<UUID, ExistingCategory> = CategoryModel
        .selectAll()
        .where { CategoryModel.id inList ids }
        .associate { it[CategoryModel.id].value to it.toExistingCategory() }

    private fun countQuestions(testIds: List<UUID>): Map<UUID, Int> {
        if (testIds.isEmpty()) return emptyMap()
        val count = QuestionModel.id.count()
        return QuestionModel
            .select(QuestionModel.testId, count)
            .where { QuestionModel.testId inList testIds }
            .groupBy(QuestionModel.testId)
            .associate { row ->
                row[QuestionModel.testId]!!.value to row[count].toInt()
            }
    }

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
