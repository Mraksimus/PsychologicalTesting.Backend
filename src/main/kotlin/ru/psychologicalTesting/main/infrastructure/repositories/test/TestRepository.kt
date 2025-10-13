package ru.psychologicalTesting.main.infrastructure.repositories.test

import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.Test
import java.util.*

interface TestRepository {

    fun create(dto: Test): Test

    fun findAll(
        offset: Long,
        limit: Int
    ): PageResponse<Test>

    fun update(
        id: UUID,
        dto: Test
    )

    fun delete(id: UUID)

}
