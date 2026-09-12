package ru.psychologicalTesting.main.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import ru.psychologicalTesting.main.infrastructure.controllers.admin.category.configureAdminCategoryRouting
import ru.psychologicalTesting.main.infrastructure.controllers.admin.role.configureAdminRoleRouting
import ru.psychologicalTesting.main.infrastructure.controllers.admin.statistics.configureAdminStatisticsRouting
import ru.psychologicalTesting.main.infrastructure.controllers.admin.survey.configureAdminSurveyRouting
import ru.psychologicalTesting.main.infrastructure.controllers.admin.survey.question.configureAdminSurveyQuestionRouting
import ru.psychologicalTesting.main.infrastructure.controllers.admin.survey.session.configureAdminSurveySessionRouting
import ru.psychologicalTesting.main.infrastructure.controllers.admin.test.configureAdminTestRouting
import ru.psychologicalTesting.main.infrastructure.controllers.admin.test.question.configureAdminQuestionRouting
import ru.psychologicalTesting.main.infrastructure.controllers.admin.test.session.configureAdminTestSessionRouting
import ru.psychologicalTesting.main.infrastructure.controllers.admin.user.configureAdminUserRouting
import ru.psychologicalTesting.main.infrastructure.controllers.authentication.configureAuthenticationRouting
import ru.psychologicalTesting.main.infrastructure.controllers.category.configureCategoryRouting
import ru.psychologicalTesting.main.infrastructure.controllers.chat.configureChatRouting
import ru.psychologicalTesting.main.infrastructure.controllers.profile.conffigureUserProfileRouting
import ru.psychologicalTesting.main.infrastructure.controllers.survey.session.configureSurveySessionRouting
import ru.psychologicalTesting.main.infrastructure.controllers.survey.survey.configureSurveyRouting
import ru.psychologicalTesting.main.infrastructure.controllers.testing.session.configureTestingSessionRouting
import ru.psychologicalTesting.main.infrastructure.controllers.testing.test.configureTestRouting

fun Application.configureRouting() = routing {
    configureAuthenticationRouting()
    configureChatRouting()
    configureTestRouting()
    configureTestingSessionRouting()
    configureSurveyRouting()
    configureSurveySessionRouting()
    conffigureUserProfileRouting()
    configureAdminRoleRouting()
    configureAdminTestRouting()
    configureAdminQuestionRouting()
    configureAdminTestSessionRouting()
    configureAdminSurveyRouting()
    configureAdminSurveyQuestionRouting()
    configureAdminSurveySessionRouting()
    configureAdminUserRouting()
    configureCategoryRouting()
    configureAdminCategoryRouting()
    configureAdminStatisticsRouting()
}
