package be.bluexin.mcui.themes.miniscript.serialization.json

import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.miniscript.CValue
import be.bluexin.mcui.themes.miniscript.CacheType
import be.bluexin.mcui.themes.miniscript.NamedExpressionIntermediate
import be.bluexin.mcui.themes.miniscript.expressionIntermediate
import be.bluexin.mcui.themes.miniscript.serialization.JelType
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class NamedExpressionJsonAdapter : TypeAdapter<Map<String, CValue<*>?>>() {
    private val logger: Logger by lazy { LogManager.getLogger(javaClass) }

    override fun write(out: JsonWriter, value: Map<String, CValue<*>?>?) {
        if (value.isNullOrEmpty()) out.nullValue()
        else {
            out.beginObject()
            value.forEach { (key, value) ->
                out.name(key)
                if (value == null) out.nullValue()
                else {
                    val ei = value.value.expressionIntermediate as NamedExpressionIntermediate
                    out.beginObject()
                        .name("type")
                        .value(ei.type.name)
                        .name("expression")
                        .value(ei.expression)
                    if (ei.cacheType != CacheType.PER_FRAME) out
                        .name("cache")
                        .value(ei.cacheType.name)
                    out.endObject()
                }
            }
            out.endObject()
        }
    }

    override fun read(reader: JsonReader): Map<String, CValue<*>?> {
        return when (val nextToken = reader.peek()) {
            JsonToken.BEGIN_OBJECT -> {
                buildMap {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        put(
                            reader.nextName(),
                            if (reader.peek() == JsonToken.NULL) null
                            else {
                                reader.beginObject()
                                val map = buildMap {
                                    while (reader.hasNext()) {
                                        put(reader.nextName(), reader.nextString())
                                    }
                                }
                                val cacheType = map["cache"]?.let(CacheType::valueOf) ?: CacheType.PER_FRAME
                                val expression = map["expression"]
                                val type = map["type"]?.let (JelType::valueOf) ?: JelType.ERROR

                                reader.endObject()
                                /*if (expression != null && type != JelType.ERROR) type.expressionAdapter.compile(NamedExpressionIntermediate().apply {
                                    this.type = type
                                    this.cacheType = cacheType
                                    this.expression = expression
                                    this.key = key
                                }) else {
                                    val message = "Unable to deserialize ${reader.path} : missing cache or expression"
                                    logger.warn(message)
                                    AbstractThemeLoader.Reporter += message
                                    null
                                }*/
                                null
                            }
                            )
                    }
                    reader.endObject()
                }
            }
            JsonToken.NULL -> emptyMap<String, CValue<*>>()
            else -> {
                val message = "Unable to deserialize ${reader.path} : Unexpected token $nextToken"
                logger.warn(message)
                AbstractThemeLoader.Reporter += message
                emptyMap()
            }
        }
    }
}