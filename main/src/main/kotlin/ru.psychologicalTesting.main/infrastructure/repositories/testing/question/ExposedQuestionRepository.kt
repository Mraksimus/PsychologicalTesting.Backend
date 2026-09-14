package ru.psychologicalTesting.main.infrastructure.repositories.testing.question

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import ru.psychologicalTesting.common.testing.question.ExistingQuestion
import ru.psychologicalTesting.common.testing.question.NewQuestion
import ru.psychologicalTesting.main.extensions.deleteById
import ru.psychologicalTesting.main.extensions.updateById
import ru.psychologicalTesting.main.infrastructure.models.testing.QuestionModel
import java.util.*

@Single
class ExposedQuestionRepository : QuestionRepository {

    override fun create(
        dto: NewQuestion
    ): ExistingQuestion {

        require((dto.testId == null) xor (dto.surveyId == null)) {
            "NewQuestion must have exactly one of testId/surveyId"
        }

        val positionExpression = QuestionModel.position.max()

        val parentPredicate = if (dto.testId != null) {
            QuestionModel.testId eq dto.testId!!
        } else {
            QuestionModel.surveyId eq dto.surveyId!!
        }

        val position = QuestionModel
            .select(positionExpression)
            .where(parentPredicate)
            .firstOrNull()
            ?.let {
                it[positionExpression]?.inc()
            }
            ?: 0

        val insertedRow = QuestionModel.insert {
            it[testId] = dto.testId
            it[surveyId] = dto.surveyId
            it[content] = dto.content
            it[this.position] = position
        }

        return ExistingQuestion(
            id = insertedRow[QuestionModel.id].value,
            testId = insertedRow[QuestionModel.testId]?.value,
            surveyId = insertedRow[QuestionModel.surveyId]?.value,
            content = insertedRow[QuestionModel.content],
            position = insertedRow[QuestionModel.position]
        )
    }

    override fun findAllByTestId(
        id: UUID
    ): List<ExistingQuestion> {
        return QuestionModel
            .selectAll()
            .where(QuestionModel.testId eq id)
            .map { it.toExistingQuestion() }
    }

    override fun findAllBySurveyId(
        id: UUID
    ): List<ExistingQuestion> {
        return QuestionModel
            .selectAll()
            .where(QuestionModel.surveyId eq id)
            .map { it.toExistingQuestion() }
    }

    override fun update(
        id: UUID,
        dto: NewQuestion
    ): Boolean {
        val affectedRows = QuestionModel.updateById(id) {
            it[content] = dto.content
        }
        return affectedRows > 0
    }

    override fun updatePositionById(
        id: UUID,
        position: Int
    ): Boolean {

        val question = QuestionModel
            .selectAll()
            .where(QuestionModel.id eq id)
            .map { it.toExistingQuestion() }
            .firstOrNull()
            ?: return false

        val oldPosition = question.position
        if (oldPosition == position) {
            return true
        }

        val parentPredicate: Op<Boolean> = when {
            question.testId != null -> QuestionModel.testId eq question.testId!!
            question.surveyId != null -> QuestionModel.surveyId eq question.surveyId!!
            else -> return false
        }

        if (position < oldPosition) {
            QuestionModel.update(
                where = {
                    parentPredicate
                        .and(QuestionModel.position greaterEq position)
                        .and(QuestionModel.position lessEq oldPosition)
                }
            ) {
                it[this.position] = QuestionModel.position + 1
            }
        } else {
            QuestionModel.update(
                where = {
                    parentPredicate
                        .and(QuestionModel.position greaterEq oldPosition)
                        .and(QuestionModel.position lessEq position)
                }
            ) {
                it[this.position] = QuestionModel.position - 1
            }
        }

        QuestionModel.updateById(id) {
            it[this.position] = position
        }

        return true
    }

    override fun delete(id: UUID): Boolean {
        return QuestionModel.deleteById(id) > 0
    }

    private fun ResultRow.toExistingQuestion() = ExistingQuestion(
        id = this[QuestionModel.id].value,
        testId = this[QuestionModel.testId]?.value,
        surveyId = this[QuestionModel.surveyId]?.value,
        content = this[QuestionModel.content],
        position = this[QuestionModel.position]
    )

}
