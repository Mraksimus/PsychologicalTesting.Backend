package ru.psychologicalTesting.main.config.database

interface DatabaseConfig {

    val driver: String
    val host: String
    val port: UShort?

    val username: String?
    val database: String?
    val password: String?

    val jdbcConnectionUrl: String
        get() = buildString {

            append("jdbc:$driver://$host")

            port?.let {
                append(":$port")
            }

            database?.let {
                append("/$database")
            }

        }

}
