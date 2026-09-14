package ru.psychologicalTesting.main.infrastructure.repositories.authentication

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.authentication.ExistingSession
import ru.psychologicalTesting.main.infrastructure.dto.authentication.NewSession
import ru.psychologicalTesting.main.infrastructure.dto.authentication.Session
import ru.psychologicalTesting.main.infrastructure.models.SessionModel
import java.util.UUID

@Single
class ExposedSessionRepository : SessionRepository {

    override fun create(session: NewSession): ExistingSession {

        val insertedRow = SessionModel.insert {
            it[userId] = session.userId
            it[userAgent] = session.userAgent
            it[ipAddress] = session.ipAddress
            it[lastLoginAt] = session.lastLoginAt
        }

        return ExistingSession(
            id = insertedRow[SessionModel.id].value,
            userId = session.userId,
            userAgent = session.userAgent,
            ipAddress = session.ipAddress,
            lastLoginAt = session.lastLoginAt
        )
    }

    override fun findById(sessionId: UUID): ExistingSession? {
        return SessionModel
            .selectAll()
            .where(SessionModel.id eq sessionId)
            .firstOrNull()
            ?.toExistingSession()
    }

    override fun updateById(sessionId: UUID, session: Session): Boolean {
        val affectedRows = SessionModel.updateById(sessionId) {
            it[userAgent] = session.userAgent
            it[ipAddress] = session.ipAddress
            it[lastLoginAt] = session.lastLoginAt
        }
        return affectedRows > 0
    }

    override fun deleteById(sessionId: UUID): Boolean {
        return SessionModel.deleteById(sessionId) > 0
    }

    override fun deleteAllByUserIdExcept(userId: UUID, sessionId: UUID): Int {
        return SessionModel.deleteWhere {
            (SessionModel.userId eq userId).and(SessionModel.id neq sessionId)
        }
    }

    private fun ResultRow.toExistingSession() = ExistingSession(
        id = get(SessionModel.id).value,
        userId = get(SessionModel.userId).value,
        userAgent = get(SessionModel.userAgent),
        ipAddress = get(SessionModel.ipAddress),
        lastLoginAt = get(SessionModel.lastLoginAt)
    )

}
