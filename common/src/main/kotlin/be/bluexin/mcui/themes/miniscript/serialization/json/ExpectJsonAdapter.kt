package be.bluexin.mcui.themes.miniscript.serialization.json

import be.bluexin.mcui.themes.elements.Expect
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.miniscript.CacheType
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ExpectJsonAdapter : TypeAdapter<Expect>() {
    private val logger: Logger by lazy { LogManager.getLogger(javaClass) }

    override fun write(out: JsonWriter, value: Expect?) {
        if (value == null || value.variables.isEmpty()) out.nullValue()
        else {
            out.beginObject()
            value.variables.forEach { (key, it) ->
                out.name(key)
                if (it.hasDefault()) {
                    out.beginObject()
                        .name("type")
                        .value(it.type.name)
                        .name("default")
                        .value(it.expression)

                    if (it.cacheType != CacheType.PER_FRAME) out
                        .name("cache")
                        .value(it.cacheType.name)
                    out.endObject()
                } else out.value(it.type.name)
            }
            out.endObject()
        }
    }

    override fun read(reader: JsonReader): Expect? = when (val nextToken = reader.peek()) {
        JsonToken.BEGIN_OBJECT -> {
            reader.beginObject()
            val e = Expect(
                emptyMap() /*buildList {
                    while (reader.hasNext()) {
                        add(NamedExpressionIntermediate().apply {
                            key = reader.nextName()
                            when (val nestedToken = reader.peek()) {
                                JsonToken.BEGIN_OBJECT -> {
                                    reader.beginObject()
                                    while (reader.hasNext()) when (reader.nextName()) {
                                        "type" -> type = reader.nextString().let(JelType::valueOf)
                                        "default" -> expression = reader.nextString()
                                        "cache" -> cacheType = reader.nextString().let(CacheType::valueOf)
                                    }
                                    reader.endObject()
                                }
                                JsonToken.STRING -> type = reader.nextString().let(JelType::valueOf)
                                else -> {
                                    val message = "Unable to deserialize ${reader.path} : Unexpected token $nestedToken"
                                    logger.warn(message)
                                    AbstractThemeLoader.Reporter += message
                                }
                            }
                        })
                    }
                }*/
            )
            reader.endObject()
            e
        }
        JsonToken.NULL -> null
        else -> {
            val message = "Unable to deserialize ${reader.path} : Unexpected token $nextToken"
            logger.warn(message)
            AbstractThemeLoader.Reporter += message
            null
        }
    }
}