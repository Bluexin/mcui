package be.bluexin.mcui.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.resources.ResourceLocation

fun ResourceLocation.append(suffix: String) = ResourceLocation(namespace, "$path$suffix")

class RLSerializer : KSerializer<ResourceLocation> {
    override fun deserialize(decoder: Decoder): ResourceLocation =
        decoder.decodeString().lowercase().let(::ResourceLocation)

    override val descriptor = PrimitiveSerialDescriptor(ResourceLocation::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ResourceLocation) =
        encoder.encodeString(value.toString())
}
