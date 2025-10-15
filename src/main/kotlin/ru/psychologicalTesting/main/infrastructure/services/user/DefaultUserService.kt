package ru.psychologicalTesting.main.infrastructure.services.user

import org.koin.core.annotation.Single
import org.mindrot.jbcrypt.BCrypt
import ru.psychologicalTesting.main.infrastructure.dto.User
import ru.psychologicalTesting.main.infrastructure.repositories.user.UserRepository
import java.util.*

@Single
class DefaultUserService(
    private val userRepository: UserRepository,
) : UserService {

    override fun create(
        email: String,
        password: String
    ): User {

        val user = userRepository.create(
            email = email,
            password = BCrypt.hashpw(password, BCrypt.gensalt())
        )

        return user
    }

    override fun delete(userId: UUID) {
        userRepository.deleteById(userId)
    }

}
