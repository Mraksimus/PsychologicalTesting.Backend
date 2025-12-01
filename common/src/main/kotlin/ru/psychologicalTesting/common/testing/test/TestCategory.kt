package ru.psychologicalTesting.common.testing.test

import kotlinx.serialization.Serializable

@Serializable
enum class TestCategory {
    PERSONALITY,
    EMOTIONS,
    INTELLECT,
    CAREER,
    RELATIONSHIPS,
    DEVELOPMENT,
    OTHER
}
