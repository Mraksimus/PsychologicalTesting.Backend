package ru.psychologicalTesting.main.infrastructure.models.testing

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.json.jsonb
import ru.psychologicalTesting.common.testing.question.QuestionContentType
import ru.psychologicalTesting.main.infrastructure.models.json
import ru.psychologicalTesting.main.infrastructure.models.survey.SurveyModel

object QuestionModel : UUIDTable("question") {
    val testId = reference("test_id", TestModel, onDelete = ReferenceOption.CASCADE).nullable()
    val surveyId = reference("survey_id", SurveyModel, onDelete = ReferenceOption.CASCADE).nullable()
    val content = jsonb<QuestionContentType>("content", json)
    val position = integer("position")
}
