package ru.psychologicalTesting.main.infrastructure.repositories.authentication

import ru.psychologicalTesting.main.infrastructure.dto.authentication.ExistingSession
import ru.psychologicalTesting.main.infrastructure.dto.authentication.NewSession
import ru.psychologicalTesting.main.infrastructure.dto.authentication.Session
import java.util.UUID

interface SessionRepository {

    fun create(session: NewSession): ExistingSession

    fun findById(sessionId: UUID): ExistingSession?

    fun updateById(sessionId: UUID, session: Session): Boolean

    fun deleteById(sessionId: UUID): Boolean

    fun deleteAllByUserIdExcept(userId: UUID, sessionId: UUID): Int

}
