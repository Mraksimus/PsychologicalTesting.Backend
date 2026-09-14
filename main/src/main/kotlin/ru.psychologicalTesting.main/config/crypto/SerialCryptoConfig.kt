package ru.psychologicalTesting.main.config.crypto

import kotlinx.serialization.Serializable

@Serializable
data class SerialCryptoConfig(
    override val key: String,
) : CryptoConfig
