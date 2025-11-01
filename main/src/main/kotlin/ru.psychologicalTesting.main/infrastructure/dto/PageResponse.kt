package ru.psychologicalTesting.main.infrastructure.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageResponse<T : Any>(
    val total: Long,
    val offset: Long,
    val limit: Int,
    val items: List<T>
)
