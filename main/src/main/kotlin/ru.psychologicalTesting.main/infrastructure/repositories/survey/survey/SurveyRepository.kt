package ru.psychologicalTesting.main.infrastructure.repositories.survey.survey

import ru.psychologicalTesting.common.survey.survey.ExistingSurvey
import ru.psychologicalTesting.common.survey.survey.NewSurvey
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import java.util.*

interface SurveyRepository {

    fun create(
        dto: NewSurvey
    ): ExistingSurvey

    fun findOneById(
        id: UUID
    ): ExistingSurvey?

    fun findAllPage(
        offset: Long,
        limit: Int,
        activeOnly: Boolean = false
    ): PageResponse<ExistingSurvey>

    fun update(
        id: UUID,
        dto: NewSurvey
    ): Boolean

    fun updatePositionById(
        id: UUID,
        position: Int
    ): Boolean

    fun delete(
        id: UUID
    ): Boolean

}
