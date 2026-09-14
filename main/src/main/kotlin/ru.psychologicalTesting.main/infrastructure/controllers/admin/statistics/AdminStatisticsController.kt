package ru.psychologicalTesting.main.infrastructure.controllers.admin.statistics

import dev.h4kt.ktorDocs.dsl.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route
import io.ktor.util.reflect.typeInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import ru.psychologicalTesting.common.statistics.ActivityPoint
import ru.psychologicalTesting.common.statistics.AdminStatistics
import ru.psychologicalTesting.common.statistics.RecentEvent
import ru.psychologicalTesting.common.statistics.TopItem
import ru.psychologicalTesting.common.survey.session.SurveySession
import ru.psychologicalTesting.common.testing.session.TestingSession
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.requirePermissions
import ru.psychologicalTesting.main.infrastructure.controllers.common.responses.ForbiddenResponse
import ru.psychologicalTesting.main.infrastructure.dto.role.Permission
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import ru.psychologicalTesting.main.infrastructure.models.category.CategoryModel
import ru.psychologicalTesting.main.infrastructure.models.survey.SurveyModel
import ru.psychologicalTesting.main.infrastructure.models.survey.SurveySessionModel
import ru.psychologicalTesting.main.infrastructure.models.testing.TestModel
import ru.psychologicalTesting.main.infrastructure.models.testing.TestingSessionModel
import ru.psychologicalTesting.main.plugins.suspendedTransaction

private const val SWAGGER_TAG = "Statistics (admin)"
private const val ACTIVITY_DAYS = 7
private const val TOP_ITEMS_LIMIT = 5
private const val RECENT_EVENTS_LIMIT = 10

fun Routing.configureAdminStatisticsRouting() = route("/admin/statistics") {
    authenticate("user") {
        configureAuthenticatedRoutes()
    }
}

private fun Route.configureAuthenticatedRoutes() {

    requirePermissions(Permission.SESSIONS_VIEW) {

        get {

            description = "Aggregated dashboard statistics"
            tags = listOf(SWAGGER_TAG)

            responses {
                HttpStatusCode.OK returns typeInfo<AdminStatistics>()
                HttpStatusCode.Forbidden returns typeInfo<ForbiddenResponse>()
            }

            handle {
                val result = suspendedTransaction {
                    collectStatistics()
                }
                call.respond(result)
            }
        }
    }
}

private fun collectStatistics(): AdminStatistics {
    val usersCount = UserModel.selectAll().count()
    val testsCount = TestModel.selectAll().count()
    val surveysCount = SurveyModel.selectAll().count()
    val categoriesCount = CategoryModel.selectAll().count()

    val completedTesting = TestingSessionModel
        .selectAll()
        .where { TestingSessionModel.status eq TestingSession.Status.COMPLETED }
        .count()

    val completedSurvey = SurveySessionModel
        .selectAll()
        .where { SurveySessionModel.status eq SurveySession.Status.COMPLETED }
        .count()

    val zone = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(zone).date
    val startDate = today.minus(ACTIVITY_DAYS - 1, DateTimeUnit.DAY)
    val startBoundary = LocalDateTime(startDate.year, startDate.month, startDate.dayOfMonth, 0, 0, 0)

    val testingByDay = closedAtByDay(
        completedFilter = TestingSessionModel.status eq TestingSession.Status.COMPLETED,
        closedColumn = TestingSessionModel.closedAt,
        table = TestingSessionModel,
        since = startBoundary,
    )
    val surveyByDay = closedAtByDay(
        completedFilter = SurveySessionModel.status eq SurveySession.Status.COMPLETED,
        closedColumn = SurveySessionModel.closedAt,
        table = SurveySessionModel,
        since = startBoundary,
    )

    val activity = (0 until ACTIVITY_DAYS).map { offset ->
        val date = startDate.plus(offset, DateTimeUnit.DAY)
        ActivityPoint(
            date = date,
            testingCount = testingByDay[date] ?: 0,
            surveyCount = surveyByDay[date] ?: 0,
        )
    }

    val testingCountExpr = TestingSessionModel.id.count()
    val topTests = (TestingSessionModel innerJoin TestModel)
        .select(TestingSessionModel.testId, TestModel.name, testingCountExpr)
        .where { TestingSessionModel.status eq TestingSession.Status.COMPLETED }
        .groupBy(TestingSessionModel.testId, TestModel.name)
        .orderBy(testingCountExpr, SortOrder.DESC)
        .limit(TOP_ITEMS_LIMIT)
        .map { row ->
            TopItem(
                id = row[TestingSessionModel.testId].value,
                name = row[TestModel.name],
                completions = row[testingCountExpr],
            )
        }

    val surveyCountExpr = SurveySessionModel.id.count()
    val topSurveys = (SurveySessionModel innerJoin SurveyModel)
        .select(SurveySessionModel.surveyId, SurveyModel.name, surveyCountExpr)
        .where { SurveySessionModel.status eq SurveySession.Status.COMPLETED }
        .groupBy(SurveySessionModel.surveyId, SurveyModel.name)
        .orderBy(surveyCountExpr, SortOrder.DESC)
        .limit(TOP_ITEMS_LIMIT)
        .map { row ->
            TopItem(
                id = row[SurveySessionModel.surveyId].value,
                name = row[SurveyModel.name],
                completions = row[surveyCountExpr],
            )
        }

    return AdminStatistics(
        usersCount = usersCount,
        testsCount = testsCount,
        surveysCount = surveysCount,
        categoriesCount = categoriesCount,
        completedTestingSessions = completedTesting,
        completedSurveySessions = completedSurvey,
        activityByDay = activity,
        topTests = topTests,
        topSurveys = topSurveys,
        recentEvents = recentEvents(),
    )
}

private fun closedAtByDay(
    completedFilter: org.jetbrains.exposed.sql.Op<Boolean>,
    closedColumn: org.jetbrains.exposed.sql.Column<LocalDateTime?>,
    table: org.jetbrains.exposed.dao.id.UUIDTable,
    since: LocalDateTime,
): Map<LocalDate, Long> {
    return table
        .select(closedColumn)
        .where { completedFilter and (closedColumn greaterEq since) }
        .mapNotNull { row -> row[closedColumn]?.date }
        .groupingBy { it }
        .eachCount()
        .mapValues { it.value.toLong() }
}

private fun recentEvents(): List<RecentEvent> {

    val testingRows = (TestingSessionModel innerJoin UserModel innerJoin TestModel)
        .select(TestModel.name, UserModel.name, UserModel.surname, TestingSessionModel.closedAt)
        .where { TestingSessionModel.status eq TestingSession.Status.COMPLETED }
        .orderBy(TestingSessionModel.closedAt, SortOrder.DESC)
        .limit(RECENT_EVENTS_LIMIT)
        .mapNotNull { row ->
            val at = row[TestingSessionModel.closedAt] ?: return@mapNotNull null
            RecentEvent(
                kind = RecentEvent.Kind.TESTING,
                userDisplayName = displayName(row[UserModel.name], row[UserModel.surname]),
                itemName = row[TestModel.name],
                at = at,
            )
        }

    val surveyRows = (SurveySessionModel innerJoin UserModel innerJoin SurveyModel)
        .select(SurveyModel.name, UserModel.name, UserModel.surname, SurveySessionModel.closedAt)
        .where { SurveySessionModel.status eq SurveySession.Status.COMPLETED }
        .orderBy(SurveySessionModel.closedAt, SortOrder.DESC)
        .limit(RECENT_EVENTS_LIMIT)
        .mapNotNull { row ->
            val at = row[SurveySessionModel.closedAt] ?: return@mapNotNull null
            RecentEvent(
                kind = RecentEvent.Kind.SURVEY,
                userDisplayName = displayName(row[UserModel.name], row[UserModel.surname]),
                itemName = row[SurveyModel.name],
                at = at,
            )
        }

    return (testingRows + surveyRows)
        .sortedByDescending { it.at }
        .take(RECENT_EVENTS_LIMIT)
}

private fun displayName(name: String, surname: String): String {
    val joined = listOf(surname, name).filter { it.isNotBlank() }.joinToString(" ")
    return joined.ifBlank { "Пользователь" }
}
