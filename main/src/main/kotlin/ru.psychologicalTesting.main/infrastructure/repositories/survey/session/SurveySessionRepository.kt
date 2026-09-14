package ru.psychologicalTesting.main.infrastructure.repositories.survey.session

import ru.psychologicalTesting.common.survey.session.ExistingSurveySession
import ru.psychologicalTesting.common.survey.session.NewSurveySession
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.survey.AdminSurveySessionItem
import java.util.*

interface SurveySessionRepository {

    fun create(
        dto: NewSurveySession
    ): ExistingSurveySession

    fun findOwnerIdBySessionId(
        sessionId: UUID
    ): UUID?

    fun findOneById(
        id: UUID
    ): ExistingSurveySession?

    fun findOneByUserIdWithSurveyId(
        userId: UUID,
        surveyId: UUID
    ): ExistingSurveySession?

    fun findAllByUserIdPaged(
        userId: UUID,
        offset: Long,
        limit: Int
    ): PageResponse<ExistingSurveySession>

    fun findAllAdminBySurveyIdPaged(
        surveyId: UUID,
        offset: Long,
        limit: Int
    ): PageResponse<AdminSurveySessionItem>

    fun update(
        id: UUID,
        dto: ExistingSurveySession
    ): Boolean

    fun delete(
        id: UUID
    ): Boolean

}
