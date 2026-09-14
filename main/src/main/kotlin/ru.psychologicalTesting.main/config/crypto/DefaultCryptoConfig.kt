package ru.psychologicalTesting.main.config.crypto

import io.ktor.server.application.Application
import io.ktor.server.config.getAs
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class DefaultCryptoConfig(
    @Provided app: Application
) : CryptoConfig by app.environment.config.property("crypto").getAs<SerialCryptoConfig>()
