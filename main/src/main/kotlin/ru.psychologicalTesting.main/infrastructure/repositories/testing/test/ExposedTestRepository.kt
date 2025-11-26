package ru.psychologicalTesting.main.infrastructure.repositories.testing.test

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.testing.test.ExistingTest
import ru.psychologicalTesting.main.infrastructure.dto.testing.test.NewTest
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
            it[createdAt] = LocalDateTime.now()
            it[this.position] = position
        }

        return ExistingTest(
            id = insertedRow[TestModel.id].value,
            name = insertedRow[TestModel.name],
            description = insertedRow[TestModel.description],
            transcript = insertedRow[TestModel.transcript],
            durationMins = insertedRow[TestModel.durationMins],
            isActive = insertedRow[TestModel.isActive],
            createdAt = insertedRow[TestModel.createdAt],
            updatedAt = insertedRow[TestModel.updatedAt],
            position = insertedRow[TestModel.position]
        )
    }

    override fun findOneById(
        id: UUID
    ): ExistingTest? {
        return TestModel
            .selectAll()
            .where(TestModel.id eq id)
            .firstOrNull()
            ?.toExistingTest()
    }

    override fun findAllPage(
        offset: Long,
        limit: Int
    ): PageResponse<ExistingTest> {

        val totalCount = TestModel
            .selectAll()
            .count()

        val tests = TestModel
            .selectAll()
            .offset(offset)
            .limit(limit)
            .map {
                it.toExistingTest()
            }
            .sortedBy {
                it.position
            }

        return PageResponse(
            total = totalCount,
            offset = offset,
            limit = limit,
            items = tests
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
            .map {
                it.toExistingTest()
            }
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
        createdAt = this[TestModel.createdAt],
        updatedAt = this[TestModel.updatedAt],
        position = this[TestModel.position]
    )

}
