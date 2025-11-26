package ru.psychologicalTesting.main.infrastructure.controllers.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class ReorderRequest(
    val position: Int
)
