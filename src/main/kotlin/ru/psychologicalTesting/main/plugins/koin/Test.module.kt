package ru.psychologicalTesting.main.plugins.koin

import ru.psychologicalTesting.main.infrastructure.repositories.test.TestRepository
import ru.psychologicalTesting.main.infrastructure.repositories.test.ExposedTestRepository
import org.koin.dsl.module

val testModule = module {
    single<TestRepository> { ExposedTestRepository() }
}
