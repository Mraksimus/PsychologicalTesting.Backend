package ru.psychologicalTesting.main.infrastructure.controllers.admin.survey.question.types.parameters

import ru.psychologicalTesting.main.infrastructure.controllers.admin.survey.types.parameters.SurveyScopeParameters

class SurveyQuestionScopeParameters : SurveyScopeParameters() {
    val questionId by path.uuid {
        name = "question_id"
        description = "Question id to manipulate"
    }
}
