package ru.psychologicalTesting.common.statistics

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.common.compat.SerialUUID

@Serializable
data class AdminStatistics(
    val usersCount: Long,
    val testsCount: Long,
    val surveysCount: Long,
    val categoriesCount: Long,
    val completedTestingSessions: Long,
    val completedSurveySessions: Long,
    val activityByDay: List<ActivityPoint>,
    val topTests: List<TopItem>,
    val topSurveys: List<TopItem>,
    val recentEvents: List<RecentEvent>,
)

@Serializable
data class ActivityPoint(
    val date: LocalDate,
    val testingCount: Long,
    val surveyCount: Long,
)

@Serializable
data class TopItem(
    val id: SerialUUID,
    val name: String,
    val completions: Long,
)

@Serializable
data class RecentEvent(
    val kind: Kind,
    val userDisplayName: String,
    val itemName: String,
    val at: LocalDateTime,
) {
    @Serializable
    enum class Kind { TESTING, SURVEY }
}
