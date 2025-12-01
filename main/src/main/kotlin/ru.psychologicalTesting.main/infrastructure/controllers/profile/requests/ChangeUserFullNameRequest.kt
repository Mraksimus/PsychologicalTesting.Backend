package ru.psychologicalTesting.main.infrastructure.controllers.profile.requests

import dev.nesk.akkurate.Validator
import dev.nesk.akkurate.annotations.Validate
import dev.nesk.akkurate.constraints.builders.hasLengthLowerThanOrEqualTo
import dev.nesk.akkurate.constraints.builders.isNotBlank
import kotlinx.serialization.Serializable
import ru.psychologicalTesting.main.infrastructure.controllers.profile.requests.validation.accessors.name
import ru.psychologicalTesting.main.infrastructure.controllers.profile.requests.validation.accessors.patronymic
import ru.psychologicalTesting.main.infrastructure.controllers.profile.requests.validation.accessors.surname

@Validate
@Serializable
data class ChangeUserFullNameRequest(
    val name: String,
    val surname: String,
    val patronymic: String? = null
) {
    companion object {

        val validator = Validator.suspendable<ChangeUserFullNameRequest> {

            name {
                isNotBlank()
                hasLengthLowerThanOrEqualTo(128)
            }

            surname {
                isNotBlank()
                hasLengthLowerThanOrEqualTo(128)
            }

            patronymic {
                hasLengthLowerThanOrEqualTo(128)
            }

        }

    }
}