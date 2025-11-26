package ru.psychologicalTesting.main.infrastructure.controllers.common.utils

import io.ktor.http.HttpHeaders
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.host

fun ApplicationRequest.actualHost(): String {
    return headers[HttpHeaders.XForwardedHost] ?: host()
}
