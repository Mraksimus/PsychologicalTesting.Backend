package ru.psychologicalTesting.main.plugins

import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import ru.psychologicalTesting.main.config.database.DatabaseConfig
import ru.psychologicalTesting.main.infrastructure.models.TokenModel
import ru.psychologicalTesting.main.infrastructure.models.UserModel
import javax.sql.DataSource

fun Application.configureDatabase() {

    val config by inject<DatabaseConfig>()

    val dataSource = HikariDataSource().apply {
        this.jdbcUrl = config.jdbcConnectionUrl
        this.username = config.username ?: ""
        this.password = config.password ?: ""
    }

    Database.connect(dataSource)
    applyDatabaseMigrations(dataSource)

}

fun applyDatabaseMigrations(
    dataSource: DataSource
) {

    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:/migrations")
        .baselineOnMigrate(true)
        .baselineVersion("1")
        .load()

    flyway.migrate()
    flyway.validate()

}

/*
 * FYI: used only to generate migration scripts to use with FlyWay
 * Should be replaced by a gradle plugin whenever it is available
 * https://youtrack.jetbrains.com/issue/EXPOSED-755/Create-a-migration-Gradle-plugin
 *
 * !!! DO NOT COMMIT ACTUAL DATABASE CREDENTIALS !!!
 * !!! DO NOT FORGET TO COMMENT IT OUT BEFORE COMMITTING !!!
 */
@OptIn(ExperimentalDatabaseMigrationApi::class)
fun main() {

    val tables: Array<Table> = arrayOf(
        TokenModel,
        UserModel
    )

    val host = System.getenv("DB_HOST") ?: "localhost"
    val port = System.getenv("DB_PORT") ?: "4343"
    val database = System.getenv("DB_NAME") ?: "postgres"
    val username = System.getenv("DB_USERNAME") ?: "postgres"
    val password = System.getenv("DB_PASSWORD") ?: "postgres"

    Database.connect(
        url = "jdbc:postgresql://$host:$port/$database",
        user = username,
        password = password
    )

    transaction {
        MigrationUtils.generateMigrationScript(
            tables = tables,
            scriptDirectory = "src/main/resources/migrations",
            // Make sure to change script name before generating, otherwise rollback the overridden file :)
            scriptName = "V1__init",
        )
    }

}

suspend fun <R> suspendedTransaction(statement: Transaction.() -> R) = newSuspendedTransaction(
    context = Dispatchers.IO,
    statement = statement
)
