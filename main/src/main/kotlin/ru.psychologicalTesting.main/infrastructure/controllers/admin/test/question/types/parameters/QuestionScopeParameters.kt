package ru.psychologicalTesting.main.infrastructure.controllers.admin.test.question.types.parameters

import ru.psychologicalTesting.main.infrastructure.controllers.admin.test.types.parameters.TestScopeParameters

class QuestionScopeParameters : TestScopeParameters() {
    val questionId by path.uuid {
        name = "question_id"
        description = "Question id to manipulate"
    }
}
