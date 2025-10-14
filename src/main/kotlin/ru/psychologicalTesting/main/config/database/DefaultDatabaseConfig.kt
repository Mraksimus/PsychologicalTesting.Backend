package ru.psychologicalTesting.main.config.database

import org.koin.core.annotation.Single

@Single
class DefaultDatabaseConfig : DatabaseConfig by SerialDatabaseConfig.load()
