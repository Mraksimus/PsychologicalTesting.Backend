package ru.psychologicalTesting.main.infrastructure.repositories.survey.session

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.survey.session.ExistingSurveySession
import ru.psychologicalTesting.common.survey.session.NewSurveySession
import ru.psychologicalTesting.common.survey.session.SurveySession
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.survey.AdminSurveySessionItem
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import ru.psychologicalTesting.main.infrastructure.models.survey.SurveySessionModel
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class ExposedSurveySessionRepository : SurveySessionRepository {

    override fun create(
        dto: NewSurveySession
    ): ExistingSurveySession {

        val insertedRow = SurveySessionModel.insert {
            it[userId] = dto.userId
            it[surveyId] = dto.surveyId
            it[answers] = emptyList()
            it[status] = SurveySession.Status.IN_PROGRESS
            it[createdAt] = LocalDateTime.now()
        }

        return ExistingSurveySession(
            id = insertedRow[SurveySessionModel.id].value,
            userId = insertedRow[SurveySessionModel.userId].value,
            surveyId = insertedRow[SurveySessionModel.surveyId].value,
            answers = insertedRow[SurveySessionModel.answers],
            status = insertedRow[SurveySessionModel.status],
            createdAt = insertedRow[SurveySessionModel.createdAt]
        )
    }

    override fun findOwnerIdBySessionId(
        sessionId: UUID
    ): UUID? {
        return SurveySessionModel
            .select(SurveySessionModel.userId)
            .where(SurveySessionModel.id eq sessionId)
            .firstOrNull()
            ?.let { it[SurveySessionModel.userId].value }
    }

    override fun findOneById(
        id: UUID
    ): ExistingSurveySession? {
        return SurveySessionModel
            .selectAll()
            .where(SurveySessionModel.id eq id)
            .firstOrNull()
            ?.toExistingSurveySession()
    }

    override fun findOneByUserIdWithSurveyId(
        userId: UUID,
        surveyId: UUID
    ): ExistingSurveySession? {
        return SurveySessionModel
            .selectAll()
            .where {
                (SurveySessionModel.userId eq userId)
                    .and(SurveySessionModel.surveyId eq surveyId)
            }
            .firstOrNull()
            ?.toExistingSurveySession()
    }

    override fun findAllByUserIdPaged(
        userId: UUID,
        offset: Long,
        limit: Int
    ): PageResponse<ExistingSurveySession> {

        val totalCount = SurveySessionModel
            .selectAll()
            .where(SurveySessionModel.userId eq userId)
            .count()

        val sessions = SurveySessionModel
            .selectAll()
            .where(SurveySessionModel.userId eq userId)
            .orderBy(SurveySessionModel.createdAt to SortOrder.DESC)
            .offset(offset)
            .limit(limit)
            .map { it.toExistingSurveySession() }

        return PageResponse(
            total = totalCount,
            offset = offset,
            limit = limit,
            items = sessions
        )
    }

    override fun findAllAdminBySurveyIdPaged(
        surveyId: UUID,
        offset: Long,
        limit: Int
    ): PageResponse<AdminSurveySessionItem> {

        val totalCount = SurveySessionModel
            .selectAll()
            .where(SurveySessionModel.surveyId eq surveyId)
            .count()

        val items = SurveySessionModel
            .innerJoin(UserModel)
            .selectAll()
            .where { SurveySessionModel.surveyId eq surveyId }
            .orderBy(SurveySessionModel.createdAt to SortOrder.DESC)
            .offset(offset)
            .limit(limit)
            .map {
                val name = it[UserModel.name]
                val surname = it[UserModel.surname]
                val patronymic = it[UserModel.patronymic].orEmpty()
                val fullName = listOf(surname, name, patronymic)
                    .filter { part -> part.isNotBlank() }
                    .joinToString(" ")
                AdminSurveySessionItem(
                    id = it[SurveySessionModel.id].value,
                    userId = it[SurveySessionModel.userId].value,
                    userFullName = fullName,
                    userEmail = it[UserModel.email],
                    surveyId = it[SurveySessionModel.surveyId].value,
                    status = it[SurveySessionModel.status],
                    answers = it[SurveySessionModel.answers],
                    createdAt = it[SurveySessionModel.createdAt],
                    closedAt = it[SurveySessionModel.closedAt],
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
        dto: ExistingSurveySession
    ): Boolean {
        val affectedRows = SurveySessionModel.updateById(id) {
            it[SurveySessionModel.answers] = dto.answers
            it[SurveySessionModel.status] = dto.status
            it[SurveySessionModel.closedAt] = dto.closedAt
        }
        return affectedRows > 0
    }

    override fun delete(
        id: UUID
    ): Boolean {
        return SurveySessionModel.deleteById(id) > 0
    }

    private fun ResultRow.toExistingSurveySession() = ExistingSurveySession(
        id = this[SurveySessionModel.id].value,
        userId = this[SurveySessionModel.userId].value,
        surveyId = this[SurveySessionModel.surveyId].value,
        answers = this[SurveySessionModel.answers],
        status = this[SurveySessionModel.status],
        createdAt = this[SurveySessionModel.createdAt],
        closedAt = this[SurveySessionModel.closedAt]
    )
}
