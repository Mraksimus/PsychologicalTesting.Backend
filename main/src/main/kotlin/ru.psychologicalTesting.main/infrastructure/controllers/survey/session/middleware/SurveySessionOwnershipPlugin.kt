package ru.psychologicalTesting.main.infrastructure.controllers.survey.session.middleware

import io.ktor.server.util.getOrFail
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.createUserOwnedPlugin
import ru.psychologicalTesting.main.infrastructure.repositories.survey.session.SurveySessionRepository
import java.util.*

val SurveySessionOwnershipPlugin = createUserOwnedPlugin(
    name = "survey-session-ownership",
    getOwnerId = getOwnerId@{ call ->

        val sessionRepository by call.inject<SurveySessionRepository>()
        val sessionId = call.parameters.getOrFail<UUID>("session_id")

        return@getOwnerId sessionRepository.findOwnerIdBySessionId(sessionId)
    }
)
