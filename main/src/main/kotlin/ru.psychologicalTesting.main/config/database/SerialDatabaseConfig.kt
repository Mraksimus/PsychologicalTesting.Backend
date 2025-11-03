package ru.psychologicalTesting.main.config.database

import kotlinx.serialization.Serializable

@Serializable
data class SerialDatabaseConfig(
    override val driver: String,
    override val host: String,
    override val port: UShort? = null,
    override val username: String? = null,
    override val database: String? = null,
    override val password: String? = null
) : DatabaseConfig
