package be.bluexin.mcui.themes.miniscript.serialization.json

import be.bluexin.mcui.themes.elements.Element
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*
import kotlin.reflect.KClass

class JsonElementAdapterFactory : TypeAdapterFactory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? =
        if (Element::class.java.isAssignableFrom(type.rawType)) JsonElementAdapter(gson) as TypeAdapter<T>
        else null
}

class JsonElementAdapter(private val gson: Gson) : TypeAdapter<Element>() {

    private val lookup: Map<String, Class<out Element>> = buildMap {
        val queue: Queue<KClass<out Element>> = LinkedList()

        var head: KClass<out Element>? = Element::class
        while (head != null) {
            if (!head.isAbstract) put(head.java.simpleName, head.java)
            /*val sa = head.findAnnotation<XmlSeeAlso>()
            if (sa != null) {
                @Suppress("UNCHECKED_CAST")
                queue.addAll(sa.value.asList() as List<KClass<out Element>>)
            }*/
            head = queue.poll()
        }
    }

    override fun write(out: JsonWriter, value: Element) {
        val json = gson.toJsonTree(value).asJsonObject
        if (value.hasParent) json.remove("name")
        out.beginObject()
        if (value.name == Element.DEFAULT_NAME) out.name(value.javaClass.simpleName)
        else out.name("${value.javaClass.simpleName}:${value.name}")
        gson.toJson(json, out)
        out.endObject()
    }

    override fun read(reader: JsonReader): Element {
        reader.beginObject()
        val idName = reader.nextName()
        val idNames = idName.split(":")
        val (id) = idNames
        val type = lookup[id] ?: error("Could not find type $id (at ${reader.path})")
        val result = gson.fromJson<Element>(reader, type)
        if (idNames.size > 1) result.name = idNames[1]
        reader.endObject()
        return result
    }
}
