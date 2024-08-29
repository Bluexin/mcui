package be.bluexin.mcui.themes.miniscript.serialization.json

import be.bluexin.mcui.config.Setting
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KClass

class JsonSettingAdapterFactory : TypeAdapterFactory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? =
        if (Setting::class.java.isAssignableFrom(type.rawType)) JsonSettingAdapter(gson) as TypeAdapter<T>
        else null

    companion object {
        val currentNamespace: ThreadLocal<ResourceLocation> = ThreadLocal.withInitial { ResourceLocation("mcui:error") }
    }
}

class JsonSettingAdapter(private val gson: Gson) : TypeAdapter<Setting<*>>() {

    private val KClass<out Setting<*>>.identifier get() = simpleName!!.substringBefore("Setting").lowercase()

    private val lookup: Map<String, KClass<out Setting<*>>> = Setting::class.sealedSubclasses
        .associateBy { it.identifier }

    override fun write(out: JsonWriter, value: Setting<*>) {
        val json = gson.toJsonTree(value).asJsonObject
        json.addProperty("type", value::class.identifier)
        json.remove("namespace")
        gson.toJson(json, out)
    }

    override fun read(reader: JsonReader): Setting<*> {
        val json = gson.fromJson<JsonObject>(reader, JsonObject::class.java)
        val type = json["type"] ?: error("Missing Setting type at ${reader.path}")
        check(type.isJsonPrimitive) { "Type has wrong type at ${reader.path}" }
        val clazz = lookup[type.asString] ?: error("Unknown type $type at ${reader.path}")
        json.addProperty("namespace", JsonSettingAdapterFactory.currentNamespace.get().toString())

        return gson.fromJson(json, clazz.java)
    }
}