package ru.psychologicalTesting.main.infrastructure.repositories.survey.survey

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.category.ExistingCategory
import ru.psychologicalTesting.common.survey.survey.ExistingSurvey
import ru.psychologicalTesting.common.survey.survey.NewSurvey
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.models.category.CategoryModel
import ru.psychologicalTesting.main.infrastructure.models.survey.SurveyModel
import ru.psychologicalTesting.main.infrastructure.models.testing.QuestionModel
import ru.psychologicalTesting.main.utils.now
import java.util.*

@Single
class ExposedSurveyRepository : SurveyRepository {

    override fun create(
        dto: NewSurvey
    ): ExistingSurvey {

        val positionExpression = SurveyModel.position.max()

        val position = SurveyModel
            .select(positionExpression)
            .firstOrNull()
            ?.let { it[positionExpression]?.inc() }
            ?: 0

        val insertedRow = SurveyModel.insert {
            it[name] = dto.name
            it[description] = dto.description
            it[durationMins] = dto.durationMins
            it[isActive] = dto.isActive
            it[categoryId] = dto.categoryId
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
            it[this.position] = position
        }

        val id = insertedRow[SurveyModel.id].value

        return ExistingSurvey(
            id = id,
            name = insertedRow[SurveyModel.name],
            description = insertedRow[SurveyModel.description],
            durationMins = insertedRow[SurveyModel.durationMins],
            isActive = insertedRow[SurveyModel.isActive],
            categoryId = insertedRow[SurveyModel.categoryId]?.value,
            category = insertedRow[SurveyModel.categoryId]?.value?.let(::loadCategory),
            questionsCount = 0,
            createdAt = insertedRow[SurveyModel.createdAt],
            updatedAt = insertedRow[SurveyModel.updatedAt],
            position = insertedRow[SurveyModel.position]
        )
    }

    override fun findOneById(id: UUID): ExistingSurvey? {
        val row = SurveyModel
            .selectAll()
            .where(SurveyModel.id eq id)
            .firstOrNull()
            ?: return null

        val survey = row.toExistingSurvey()
        val category = survey.categoryId?.let(::loadCategory)
        val count = countQuestions(listOf(survey.id))[survey.id] ?: 0
        return survey.copy(category = category, questionsCount = count)
    }

    override fun findAllPage(
        offset: Long,
        limit: Int,
        activeOnly: Boolean
    ): PageResponse<ExistingSurvey> {

        fun query(): Query = if (activeOnly) {
            SurveyModel.selectAll().where { SurveyModel.isActive eq true }
        } else {
            SurveyModel.selectAll()
        }

        val totalCount = query().count()

        val surveys = query()
            .offset(offset)
            .limit(limit)
            .map { it.toExistingSurvey() }
            .sortedBy { it.position }

        val categoryIds = surveys.mapNotNull { it.categoryId }.toSet()
        val categoriesById = if (categoryIds.isEmpty()) emptyMap() else loadCategories(categoryIds)
        val counts = countQuestions(surveys.map { it.id })

        val enriched = surveys.map {
            it.copy(
                category = it.categoryId?.let(categoriesById::get),
                questionsCount = counts[it.id] ?: 0
            )
        }

        return PageResponse(
            total = totalCount,
            offset = offset,
            limit = limit,
            items = enriched
        )
    }

    override fun update(
        id: UUID,
        dto: NewSurvey
    ): Boolean {
        val affectedRows = SurveyModel.updateById(id) {
            it[name] = dto.name
            it[description] = dto.description
            it[durationMins] = dto.durationMins
            it[isActive] = dto.isActive
            it[categoryId] = dto.categoryId
            it[updatedAt] = LocalDateTime.now()
        }
        return affectedRows > 0
    }

    override fun updatePositionById(
        id: UUID,
        position: Int
    ): Boolean {

        val survey = SurveyModel
            .selectAll()
            .where(SurveyModel.id eq id)
            .map { it.toExistingSurvey() }
            .firstOrNull()
            ?: return false

        val oldPosition = survey.position
        if (oldPosition == position) {
            return true
        }

        if (position < oldPosition) {
            SurveyModel.update(
                where = {
                    (SurveyModel.position greaterEq position)
                        .and(SurveyModel.position lessEq oldPosition)
                }
            ) {
                it[this.position] = SurveyModel.position + 1
            }
        } else {
            SurveyModel.update(
                where = {
                    (SurveyModel.position greaterEq oldPosition)
                        .and(SurveyModel.position lessEq position)
                }
            ) {
                it[this.position] = SurveyModel.position - 1
            }
        }

        SurveyModel.updateById(id) {
            it[this.position] = position
        }

        return true
    }

    override fun delete(id: UUID): Boolean {
        return SurveyModel.deleteById(id) > 0
    }

    private fun ResultRow.toExistingSurvey() = ExistingSurvey(
        id = this[SurveyModel.id].value,
        name = this[SurveyModel.name],
        description = this[SurveyModel.description],
        durationMins = this[SurveyModel.durationMins],
        isActive = this[SurveyModel.isActive],
        categoryId = this[SurveyModel.categoryId]?.value,
        category = null,
        questionsCount = 0,
        createdAt = this[SurveyModel.createdAt],
        updatedAt = this[SurveyModel.updatedAt],
        position = this[SurveyModel.position]
    )

    private fun loadCategory(id: UUID): ExistingCategory? = CategoryModel
        .selectAll()
        .where(CategoryModel.id eq id)
        .firstOrNull()
        ?.toExistingCategory()

    private fun loadCategories(ids: Set<UUID>): Map<UUID, ExistingCategory> = CategoryModel
        .selectAll()
        .where { CategoryModel.id inList ids }
        .associate { it[CategoryModel.id].value to it.toExistingCategory() }

    private fun countQuestions(surveyIds: List<UUID>): Map<UUID, Int> {
        if (surveyIds.isEmpty()) return emptyMap()
        val count = QuestionModel.id.count()
        return QuestionModel
            .select(QuestionModel.surveyId, count)
            .where { QuestionModel.surveyId inList surveyIds }
            .groupBy(QuestionModel.surveyId)
            .associate { row ->
                row[QuestionModel.surveyId]!!.value to row[count].toInt()
            }
    }

    private fun ResultRow.toExistingCategory() = ExistingCategory(
        id = this[CategoryModel.id].value,
        name = this[CategoryModel.name],
        color = this[CategoryModel.color],
        icon = this[CategoryModel.icon],
        position = this[CategoryModel.position],
        createdAt = this[CategoryModel.createdAt],
        updatedAt = this[CategoryModel.updatedAt],
    )
}
