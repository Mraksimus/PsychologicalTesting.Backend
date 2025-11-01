package ru.psychologicalTesting.main.extensions

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update

fun <TId : Comparable<TId>, TTable : IdTable<TId>> TTable.updateById(
    id: TId,
    body: TTable.(UpdateStatement) -> Unit
) = update(
    where = { this@updateById.id eq id },
    body = body
)

fun <T : Comparable<T>> IdTable<T>.deleteById(id: T) = deleteWhere { this@deleteById.id eq id }
