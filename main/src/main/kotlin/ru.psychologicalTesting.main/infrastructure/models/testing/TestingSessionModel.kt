package ru.psychologicalTesting.main.infrastructure.models.testing

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import ru.psychologicalTesting.common.testing.question.ExistingQuestion
import ru.psychologicalTesting.common.testing.session.TestingSession
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import ru.psychologicalTesting.main.infrastructure.models.json

object TestingSessionModel : UUIDTable("testing_session") {
    val userId = reference("user_id", UserModel, onDelete = ReferenceOption.CASCADE)
    val testId = reference("test_id", TestModel, onDelete = ReferenceOption.CASCADE)
    val questionResponses = jsonb<List<ExistingQuestion>>("question_responses", json)
    val result = text("result").nullable()
    val status = enumerationByName<TestingSession.Status>("status", 11)
    val createdAt = datetime("created_at")
    val closedAt = datetime("closed_at").nullable()
}
