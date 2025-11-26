package ru.psychologicalTesting.main.infrastructure.controllers.testing.session.middleware

import io.ktor.server.util.getOrFail
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.infrastructure.controllers.common.middleware.createUserOwnedPlugin
import ru.psychologicalTesting.main.infrastructure.repositories.testing.session.TestingSessionRepository
import java.util.*

val TestingSessionOwnershipPlugin = createUserOwnedPlugin(
    name = "post-ownership",
    getOwnerId = getOwnerId@{ call ->

        val testingSessionRepository by call.inject<TestingSessionRepository>()
        val sessionId = call.parameters.getOrFail<UUID>("session_id")

        return@getOwnerId testingSessionRepository.findOwnerIdBySessionId(sessionId)
    }
)
