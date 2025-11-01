package ru.psychologicalTesting.main.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import java.io.File
import com.typesafe.config.ConfigFactory as HoconConfigFactory

@OptIn(ExperimentalSerializationApi::class)
abstract class ConfigFactory<T : Any>(
    protected val defaultPath: String,
    protected val format: SerialFormat = Hocon {}
) {

    abstract fun load(
        path: String = defaultPath
    ): T

    protected inline fun <reified T : Any> deserialize(
        path: String
    ): T {
        val file = File(path)
        return when (format) {
            is Hocon -> {
                val config = HoconConfigFactory.parseFile(file)
                format.decodeFromConfig(config)
            }
            is StringFormat -> {
                format.decodeFromString(file.readText())
            }
            is BinaryFormat -> {
                format.decodeFromByteArray(file.readBytes())
            }
            else -> {
                throw UnsupportedOperationException("Unsupported serial format: ${format::class.simpleName}")
            }
        }
    }

}
