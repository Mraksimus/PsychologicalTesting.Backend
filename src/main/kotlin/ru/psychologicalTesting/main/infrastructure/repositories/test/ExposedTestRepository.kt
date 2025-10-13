package ru.psychologicalTesting.main.infrastructure.repositories.test

import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.Test
import ru.psychologicalTesting.main.infrastructure.models.TestModel
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import java.util.*

@Single
class ExposedTestRepository : TestRepository {

    override fun create(dto: Test): Test {

        val insertedRow = TestModel.insert {
            it[this.value]
        }

        return dto.copy(id = insertedRow[TestModel.id].value)
    }

    override fun findAll(
        offset: Long,
        limit: Int
    ): PageResponse<Test> {

        val total = TestModel.selectAll().count()
        val items = TestModel.selectAll()
            .limit(limit)
            .offset(offset)
            .map {
                Test(
                    id = it[TestModel.id].value,
                    value = it[TestModel.value]
                )
            }

        return PageResponse(
            total = total,
            offset = offset,
            limit = limit,
            items = items
        )
    }

    override fun update(
        id: UUID,
        dto: Test
    ) {

        TestModel.updateById(id) {
            it[this.value] = dto.value
        }

    }

    override fun delete(id: UUID) {
        TestModel.deleteById(id)
    }

}
