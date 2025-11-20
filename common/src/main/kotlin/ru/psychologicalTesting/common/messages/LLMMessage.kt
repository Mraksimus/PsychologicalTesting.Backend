package ru.psychologicalTesting.common.messages

import kotlinx.serialization.Serializable

@Serializable
data class LLMMessage(
    val role: Role,
    val content: String
) {

    @Serializable
    enum class Role {
        SYSTEM,
        USER,
        ASSISTANT
    }

}
