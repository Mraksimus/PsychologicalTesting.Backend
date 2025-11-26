package ru.psychologicalTesting.main.infrastructure.repositories.testing.question

import ru.psychologicalTesting.main.infrastructure.dto.testing.question.ExistingQuestion
import ru.psychologicalTesting.main.infrastructure.dto.testing.question.NewQuestion
import java.util.*

interface QuestionRepository {

    fun create(
        dto: NewQuestion
    ): ExistingQuestion

    fun findAllByTestId(
        id: UUID
    ): List<ExistingQuestion>

    fun update(
        id: UUID,
        dto: NewQuestion
    ): Boolean

    fun updatePositionById(
        id: UUID,
        position: Int
    ): Boolean

    fun delete(
        id: UUID
    ): Boolean

}
