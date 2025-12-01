package ru.psychologicalTesting.main.infrastructure.services.user

import org.koin.core.annotation.Single
import org.mindrot.jbcrypt.BCrypt
import ru.psychologicalTesting.main.infrastructure.dto.PageResponse
import ru.psychologicalTesting.main.infrastructure.dto.TestingSessionCard
import ru.psychologicalTesting.main.infrastructure.dto.user.User
import ru.psychologicalTesting.main.infrastructure.dto.user.UserProfile
import ru.psychologicalTesting.main.infrastructure.repositories.testing.session.TestingSessionRepository
import ru.psychologicalTesting.main.infrastructure.repositories.testing.test.TestRepository
import ru.psychologicalTesting.main.infrastructure.repositories.user.UserRepository
import ru.psychologicalTesting.main.infrastructure.services.user.results.ChangeUserFullNameResult
import ru.psychologicalTesting.main.infrastructure.services.user.results.GetUserProfileResult
import java.util.*

@Single
class DefaultUserService(
    private val userRepository: UserRepository,
    private val sessionRepository: TestingSessionRepository,
    private val testRepository: TestRepository
) : UserService {

    override fun create(
        name: String,
        surname: String,
        patronymic: String?,
        email: String,
        password: String
    ): User {

        val user = userRepository.create(
            name = name,
            surname = surname,
            patronymic = patronymic,
            email = email,
            password = BCrypt.hashpw(password, BCrypt.gensalt())
        )

        return user
    }

    override fun getUserProfileById(userId: UUID): GetUserProfileResult {

        val user = userRepository.findByUserId(userId) ?: return GetUserProfileResult.UserNotFound

        return GetUserProfileResult.Success(
            userProfile = UserProfile(
                name = user.name,
                surname = user.surname,
                patronymic = user.patronymic,
                email = user.email,
                registeredAt = user.registeredAt,
                lastLoginAt = user.lastLoginAt
            )
        )
    }

    override fun getAllSessionCardsByUserIdPaged(
        userId: UUID,
        offset: Long,
        limit: Int
    ): PageResponse<TestingSessionCard> {

        val sessionsPaged = sessionRepository.findAllByUserIdPaged(
            userId,
            offset,
            limit
        )

        val sessionCards = sessionsPaged.items.map { session ->

            val test = testRepository.findOneById(session.testId)!!

            TestingSessionCard(
                id = session.id,
                testName = test.name,
                status = session.status,
                createdAt = session.createdAt
            )
        }

        return PageResponse(
            offset = sessionsPaged.offset,
            limit = sessionsPaged.limit,
            items = sessionCards,
            total = sessionsPaged.total
        )
    }

    override fun changeUserFullName(
        userId: UUID,
        name: String,
        surname: String,
        patronymic: String?
    ): ChangeUserFullNameResult {

        val user = userRepository.findByUserId(userId) ?: return ChangeUserFullNameResult.UserNotFound

        val isUpdated = userRepository.update(
            user = user.copy(
                name = name,
                surname = surname,
                patronymic = patronymic
            )
        )

        if (!isUpdated) {
            return ChangeUserFullNameResult.UserFullNameWasNotUpdated
        }

        return ChangeUserFullNameResult.Success
    }

    override fun delete(userId: UUID): Boolean {
        return userRepository.deleteById(userId)
    }

}
