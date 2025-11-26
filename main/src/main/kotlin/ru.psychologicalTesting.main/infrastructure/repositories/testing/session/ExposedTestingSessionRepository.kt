package ru.psychologicalTesting.main.infrastructure.repositories.testing.session

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.common.testing.session.ExistingTestingSession
import ru.psychologicalTesting.common.testing.session.NewTestingSession
import ru.psychologicalTesting.common.testing.session.TestingSession
import ru.psychologicalTesting.main.infrastructure.models.testing.TestingSessionModel
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class ExposedTestingSessionRepository : TestingSessionRepository {

    override fun create(
        dto: NewTestingSession
    ): ExistingTestingSession {

        val insertedRow = TestingSessionModel.insert {
            it[userId] = dto.userId
            it[testId] = dto.testId
            it[questionResponses] = dto.questionResponses
            it[status] = TestingSession.Status.IN_PROGRESS
            it[createdAt] = LocalDateTime.now()
        }

        return ExistingTestingSession(
            id = insertedRow[TestingSessionModel.id].value,
            userId = insertedRow[TestingSessionModel.userId].value,
            testId = insertedRow[TestingSessionModel.testId].value,
            questionResponses = insertedRow[TestingSessionModel.questionResponses],
            status = insertedRow[TestingSessionModel.status],
            createdAt = insertedRow[TestingSessionModel.createdAt]
        )
    }

    override fun findOwnerIdBySessionId(
        sessionId: UUID
    ): UUID? {
        return TestingSessionModel
            .select(TestingSessionModel.userId)
            .where(TestingSessionModel.id eq sessionId)
            .firstOrNull()
            ?.let {
                it[TestingSessionModel.userId].value
            }
    }

    override fun findOneById(
        id: UUID
    ): ExistingTestingSession? {
        return TestingSessionModel
            .selectAll()
            .where(TestingSessionModel.id eq id)
            .firstOrNull()
            ?.toExistingTestingSession()
    }

    override fun findOneByUserIdWithTestId(
        userId: UUID,
        testId: UUID
    ): ExistingTestingSession? {
        return TestingSessionModel
            .selectAll()
            .where {
                (TestingSessionModel.userId eq userId)
                    .and(TestingSessionModel.testId eq testId)
            }
            .firstOrNull()
            ?.toExistingTestingSession()
    }

    override fun findAllByUserIdPaged(
        userId: UUID,
        offset: Long,
        limit: Int
    ): PageResponse<ExistingTestingSession> {

        val totalCount = TestingSessionModel
            .selectAll()
            .where(TestingSessionModel.userId eq userId)
            .count()

        val sessions = TestingSessionModel
            .selectAll()
            .where(TestingSessionModel.userId eq userId)
            .offset(offset)
            .limit(limit)
            .map {
                it.toExistingTestingSession()
            }
            .sortedByDescending {
                it.createdAt
            }

        return PageResponse(
            total = totalCount,
            offset = offset,
            limit = limit,
            items = sessions
        )
    }

    override fun update(
        id: UUID,
        dto: ExistingTestingSession
    ): Boolean {

        val affectedRows = TestingSessionModel.updateById(id) {
            it[TestingSessionModel.questionResponses] = dto.questionResponses
            it[TestingSessionModel.result] = dto.result
            it[TestingSessionModel.status] = dto.status
            it[TestingSessionModel.closedAt] = dto.closedAt
        }

        return affectedRows > 0
    }

    override fun delete(
        id: UUID
    ): Boolean {
        return TestingSessionModel.deleteById(id) > 0
    }

    private fun ResultRow.toExistingTestingSession() = ExistingTestingSession(
        id = this[TestingSessionModel.id].value,
        userId = this[TestingSessionModel.userId].value,
        testId = this[TestingSessionModel.testId].value,
        questionResponses = this[TestingSessionModel.questionResponses],
        result = this[TestingSessionModel.result],
        status = this[TestingSessionModel.status],
        createdAt = this[TestingSessionModel.createdAt],
        closedAt = this[TestingSessionModel.closedAt]
    )

}
