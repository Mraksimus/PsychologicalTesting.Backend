package ru.psychologicalTesting.main.infrastructure.models.survey

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ru.psychologicalTesting.common.survey.session.SurveySession
import ru.psychologicalTesting.common.testing.session.SessionAnswer
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import ru.psychologicalTesting.main.infrastructure.models.json

object SurveySessionModel : UUIDTable("survey_session") {
    val userId = reference("user_id", UserModel, onDelete = ReferenceOption.CASCADE)
    val surveyId = reference("survey_id", SurveyModel, onDelete = ReferenceOption.CASCADE)
    val answers = jsonb<List<SessionAnswer>>("answers", json)
    val status = enumerationByName<SurveySession.Status>("status", 11)
    val createdAt = datetime("created_at")
    val closedAt = datetime("closed_at").nullable()
}
