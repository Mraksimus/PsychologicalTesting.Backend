package ru.psychologicalTesting.common.compat

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration

typealias DurationString = @Serializable(DurationStringSerializer::class) Duration

object DurationStringSerializer : KSerializer<Duration> {

    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "DurationString",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: Duration
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Duration {
        return decoder.decodeString().let(Duration::parse)
    }

}
