package me.parrot.mirai.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * e621
 * me.parrot.mirai.serializer.DateSerializer
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 20:51
 */
object DateSerializer : KSerializer<Date> {

    private val formatter by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(this::class.java.canonicalName, PrimitiveKind.STRING)

    // 2023-10-31T19:37:23.783+08:00
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(formatter.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        return formatter.parse(decoder.decodeString())
    }

}