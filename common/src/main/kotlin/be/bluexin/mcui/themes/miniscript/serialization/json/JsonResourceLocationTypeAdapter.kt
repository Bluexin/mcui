package be.bluexin.mcui.themes.miniscript.serialization.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.resources.ResourceLocation

class JsonResourceLocationTypeAdapter : TypeAdapter<ResourceLocation>() {
    override fun write(out: JsonWriter, value: ResourceLocation) {
        out.jsonValue(value.toString())
    }

    override fun read(reader: JsonReader) = ResourceLocation(reader.nextString())
}