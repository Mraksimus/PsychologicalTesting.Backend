package ru.psychologicalTesting.main.infrastructure.repositories.testing.session

import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.testing.session.ExistingTestingSession
import ru.psychologicalTesting.main.infrastructure.dto.testing.session.NewTestingSession
import java.util.*

interface TestingSessionRepository {

    fun create(
        dto: NewTestingSession
    ): ExistingTestingSession

    fun findOwnerIdBySessionId(
        sessionId: UUID
    ): UUID?

    fun findOneById(
        id: UUID
    ): ExistingTestingSession?

    fun findOneByTestId(
        testId: UUID
    ): ExistingTestingSession?

    fun findAllByUserIdPaged(
        userId: UUID,
        offset: Long,
        limit: Int
    ): PageResponse<ExistingTestingSession>

    fun update(
        id: UUID,
        dto: ExistingTestingSession
    ): Boolean

    fun delete(
        id: UUID
    ): Boolean

}
