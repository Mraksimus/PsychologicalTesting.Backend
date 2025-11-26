package ru.psychologicalTesting.main.infrastructure.repositories.testing.session

import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.common.testing.session.ExistingTestingSession
import ru.psychologicalTesting.common.testing.session.NewTestingSession
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

    fun findOneByUserIdWithTestId(
        userId: UUID,
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
