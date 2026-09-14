package ru.psychologicalTesting.main.infrastructure.repositories.testing.session

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.testing.session.ExistingTestingSession
import ru.psychologicalTesting.common.testing.session.NewTestingSession
import ru.psychologicalTesting.common.testing.session.SessionAnswer
import ru.psychologicalTesting.common.testing.session.TestingSession
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.AdminSessionItem
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import ru.psychologicalTesting.main.infrastructure.models.testing.TestingSessionModel
import ru.psychologicalTesting.main.infrastructure.services.crypto.CryptoService
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class ExposedTestingSessionRepository(
    private val crypto: CryptoService,
) : TestingSessionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val answersSerializer = ListSerializer(SessionAnswer.serializer())

    private fun encodeAnswers(answers: List<SessionAnswer>): String =
        crypto.encrypt(json.encodeToString(answersSerializer, answers))

    private fun decodeAnswers(stored: String): List<SessionAnswer> {
        if (stored.isBlank()) return emptyList()
        val plaintext = crypto.tryDecrypt(stored)
        return runCatching { json.decodeFromString(answersSerializer, plaintext) }
            .getOrDefault(emptyList())
    }

    private fun encryptResult(value: String?): String? = value?.let(crypto::encrypt)

    private fun decryptResult(stored: String?): String? = stored?.let(crypto::tryDecrypt)

    override fun create(
        dto: NewTestingSession
    ): ExistingTestingSession {

        val insertedRow = TestingSessionModel.insert {
            it[userId] = dto.userId
            it[testId] = dto.testId
            it[answers] = encodeAnswers(emptyList())
            it[status] = TestingSession.Status.IN_PROGRESS
            it[createdAt] = LocalDateTime.now()
        }

        return ExistingTestingSession(
            id = insertedRow[TestingSessionModel.id].value,
            userId = insertedRow[TestingSessionModel.userId].value,
            testId = insertedRow[TestingSessionModel.testId].value,
            answers = emptyList(),
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

    override fun findAllByUserId(
        userId: UUID
    ): List<ExistingTestingSession> {
        return TestingSessionModel
            .selectAll()
            .where(TestingSessionModel.userId eq userId)
            .map {
                it.toExistingTestingSession()
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
            .orderBy(TestingSessionModel.createdAt to SortOrder.DESC)
            .offset(offset)
            .limit(limit)
            .map {
                it.toExistingTestingSession()
            }

        return PageResponse(
            total = totalCount,
            offset = offset,
            limit = limit,
            items = sessions
        )
    }

    override fun findAllAdminByTestIdPaged(
        testId: UUID,
        offset: Long,
        limit: Int
    ): PageResponse<AdminSessionItem> {

        val totalCount = TestingSessionModel
            .selectAll()
            .where(TestingSessionModel.testId eq testId)
            .count()

        val items = TestingSessionModel
            .innerJoin(UserModel)
            .selectAll()
            .where { TestingSessionModel.testId eq testId }
            .orderBy(TestingSessionModel.createdAt to SortOrder.DESC)
            .offset(offset)
            .limit(limit)
            .map {
                val name = it[UserModel.name]
                val surname = it[UserModel.surname]
                val patronymic = it[UserModel.patronymic].orEmpty()
                val fullName = listOf(surname, name, patronymic)
                    .filter { part -> part.isNotBlank() }
                    .joinToString(" ")
                AdminSessionItem(
                    id = it[TestingSessionModel.id].value,
                    userId = it[TestingSessionModel.userId].value,
                    userFullName = fullName,
                    userEmail = it[UserModel.email],
                    testId = it[TestingSessionModel.testId].value,
                    status = it[TestingSessionModel.status],
                    result = decryptResult(it[TestingSessionModel.result]),
                    createdAt = it[TestingSessionModel.createdAt],
                    closedAt = it[TestingSessionModel.closedAt]
                )
            }

        return PageResponse(
            total = totalCount,
            offset = offset,
            limit = limit,
            items = items
        )
    }

    override fun update(
        id: UUID,
        dto: ExistingTestingSession
    ): Boolean {

        val affectedRows = TestingSessionModel.updateById(id) {
            it[TestingSessionModel.answers] = encodeAnswers(dto.answers)
            it[TestingSessionModel.result] = encryptResult(dto.result)
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
        answers = decodeAnswers(this[TestingSessionModel.answers]),
        result = decryptResult(this[TestingSessionModel.result]),
        status = this[TestingSessionModel.status],
        createdAt = this[TestingSessionModel.createdAt],
        closedAt = this[TestingSessionModel.closedAt]
    )

}
