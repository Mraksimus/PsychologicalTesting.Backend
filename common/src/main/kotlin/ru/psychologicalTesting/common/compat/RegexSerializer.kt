package ru.psychologicalTesting.common.compat

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias SerialRegex = @Serializable(RegexSerializer::class) Regex

object RegexSerializer : KSerializer<Regex> {

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "Regex",
        kind = PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): Regex {
        return decoder.decodeString().run(String::toRegex)
    }

    override fun serialize(
        encoder: Encoder,
        value: Regex
    ) {
        encoder.encodeString(value.toString())
    }

}
