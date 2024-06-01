package be.bluexin.mcui.themes.miniscript.serialization.json

import be.bluexin.mcui.themes.miniscript.serialization.AfterDeserialization
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class AfterUnmarshalAdapterFactory : TypeAdapterFactory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? =
        if (AfterDeserialization::class.java.isAssignableFrom(type.rawType)) JsonFragmentAdapter(
            gson.getDelegateAdapter(this, type) as TypeAdapter<AfterDeserialization>
        ) as TypeAdapter<T>
        else null
}

class JsonFragmentAdapter<T: AfterDeserialization>(
    private val gson: TypeAdapter<T>
) : TypeAdapter<T>() {

    override fun write(out: JsonWriter, value: T) = gson.write(out, value)

    override fun read(reader: JsonReader) = gson.read(reader)?.apply(AfterDeserialization::afterDeserialization)
}