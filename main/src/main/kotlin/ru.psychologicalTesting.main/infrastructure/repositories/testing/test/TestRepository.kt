package ru.psychologicalTesting.main.infrastructure.repositories.testing.test

import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.testing.test.ExistingTest
import ru.psychologicalTesting.main.infrastructure.dto.testing.test.NewTest
import java.util.*

interface TestRepository {

    fun create(
        dto: NewTest
    ): ExistingTest

    fun findOneById(
        id: UUID
    ): ExistingTest?

    fun findAllPage(
        offset: Long,
        limit: Int
    ): PageResponse<ExistingTest>

    fun update(
        id: UUID,
        dto: NewTest
    ): Boolean

    fun updatePositionById(
        id: UUID,
        position: Int
    ): Boolean

    fun delete(
        id: UUID
    ): Boolean

}
