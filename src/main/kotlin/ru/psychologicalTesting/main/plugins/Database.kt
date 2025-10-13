package ru.psychologicalTesting.main.plugins

import io.ktor.server.application.Application
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Application.configureDatabase() {
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
}

suspend fun <R> suspendedTransaction(statement: Transaction.() -> R) = newSuspendedTransaction(
    context = Dispatchers.IO,
    statement = statement
)
