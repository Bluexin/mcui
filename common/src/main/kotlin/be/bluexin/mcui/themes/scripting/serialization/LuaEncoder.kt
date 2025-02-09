package be.bluexin.mcui.themes.scripting.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua

@OptIn(ExperimentalSerializationApi::class)
sealed class AbstractLuaEncoder(protected val parent: AbstractLuaEncoder? = null) : AbstractEncoder() {

    override val serializersModule = EmptySerializersModule()

    abstract fun endChild(value: Any?)

    override fun encodeValue(value: Any) = endChild(value)

    override fun encodeNull() = endChild(null)

    override fun beginStructure(descriptor: SerialDescriptor) = when (descriptor.kind) {
        StructureKind.LIST -> LuaListEncoder(this)
        StructureKind.MAP -> LuaMapEncoder(this)
        is PolymorphicKind -> LuaEncoder(this)
        else -> LuaEncoder(this)
    }

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int) = false

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) =
        encodeValue(enumDescriptor.getElementName(index))

    open class LuaEncoder(parent: AbstractLuaEncoder? = null) : AbstractLuaEncoder(parent) {

        protected var currentName: String? = null
        protected val cn get() = currentName ?: error("currentName should not be null")
        protected val properties = LuaTable()
        val data: LuaValue get() = properties

        override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
            currentName = descriptor.getElementName(index)
            return true
        }

        override fun endChild(value: Any?) {
            properties[cn] = value.asLua
            currentName = null
        }

        override fun beginStructure(descriptor: SerialDescriptor) =
            (if (currentName == null) this
            else super.beginStructure(descriptor))

        override fun endStructure(descriptor: SerialDescriptor) {
            parent?.let {
                when {
                    // maps should always get serialized in full
                    descriptor.kind == StructureKind.MAP
                            || properties.keyCount() > 1 -> it.endChild(properties)
                    // Single non-optional can get serialized as direct value
                    properties.keyCount() == 1 -> it.endChild(properties[properties.keys().single()])
                    else -> it.endChild(LuaTable())
                }
            }
        }
    }

    class LuaListEncoder(parent: AbstractLuaEncoder) : AbstractLuaEncoder(parent) {

        private val values = LuaTable()

        override fun endStructure(descriptor: SerialDescriptor) {
            parent?.endChild(values)
        }

        override fun endChild(value: Any?) {
            // In Lua, we count from 1 so pos 0 means "add to the end"
            values.insert(0, value.asLua)
        }
    }

    class LuaMapEncoder(parent: AbstractLuaEncoder) : LuaEncoder(parent) {
        private var nextIsKey = false

        override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
            nextIsKey = index % 2 == 0
            return true
        }

        override fun endChild(value: Any?) {
            if (nextIsKey) {
                require(value != null)
                currentName = value.toString()
            } else super.endChild(value)
        }
    }
}

private val Any?.asLua: LuaValue get() = if (this is LuaValue) this else CoerceJavaToLua.coerce(this)
