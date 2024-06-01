package be.bluexin.mcui.themes.scripting.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceLuaToJava
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
sealed class AbstractLuaDecoder(
    protected var parent: AbstractLuaDecoder? = null
) : AbstractDecoder() {
    override val serializersModule = EmptySerializersModule()

    protected abstract val popValue: Any?

    override fun decodeValue(): Any = when (val value = popValue) {
        null -> error("Unexpected null value")
        is LuaValue -> CoerceLuaToJava.coerce(value, Any::class.java)
        else -> value
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (descriptor.kind) {
            StructureKind.LIST -> LuaListDecoder(popValue as LuaValue, this)
            StructureKind.MAP -> LuaMapDecoder(popValue as LuaValue, this, descriptor)
            else -> {
                if (descriptor.elementsCount == 1 && descriptor.getElementDescriptor(0).kind == StructureKind.LIST) {
                    // we use a listDecoder directly when there is only 1 child, and it is a list
                    LuaListDecoder(popValue as LuaValue, this)
                } else if (descriptor.elementsCount == 1 && descriptor.getElementDescriptor(0).kind == StructureKind.MAP) {
                    // we use a mapDecoder directly when there is only 1 child, and it is a map
                    LuaMapDecoder(popValue as LuaValue, this, descriptor.getElementDescriptor(0))
                } else when (val value = popValue) {
                    is LuaTable -> {
                        if (value.keys().all { it.isnumber() }) LuaListDecoder(value, this)
                        else LuaDecoder(value, this, descriptor)
                    }

                    else -> LuaDirectDecoder(value, this)
                }
            }
        }
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor) =
        enumDescriptor.getElementIndex(popValue.asJava as String)

    open class LuaDecoder(
        queue: Deque<Map.Entry<*, *>>,
        parent: AbstractLuaDecoder?,
        descriptor: SerialDescriptor?
    ) : AbstractLuaDecoder(parent) {
        constructor(
            luaValue: LuaValue,
            parent: AbstractLuaDecoder? = null,
            descriptor: SerialDescriptor? = null
        ) : this(
            toQueue(luaValue),
            parent,
            descriptor
        )

        protected val queue: Deque<Map.Entry<*, *>> = when (descriptor?.kind) {
            null -> queue
            is PolymorphicKind -> queue.sortedBy { descriptor.getElementIndex(it.key.asJava as String) }
                .let(::LinkedList)

            else -> {
                queue.sortedBy { (key, _) ->
                    val index = descriptor.getElementIndex(key.asJava as String)
                    if (index < 0) Int.MAX_VALUE
                    else descriptor.getElementAnnotations(index)
                        .firstNotNullOfOrNull { (it as? DeserializationOrder)?.order }
                        ?: Int.MAX_VALUE
                }.let(::LinkedList)
            }
        }


        private val peekKey get() = queue.peek()?.key as? String
        override val popValue get() = queue.pop().value

        override fun decodeElementIndex(descriptor: SerialDescriptor) =
            (peekKey?.let(descriptor::getElementIndex) ?: CompositeDecoder.DECODE_DONE).also {
                require(it != CompositeDecoder.UNKNOWN_NAME) { "Unknown $descriptor in $this" }
            }

        override fun beginStructure(descriptor: SerialDescriptor) =
            // From the root decoder, we do not have a descriptor yet -- this will apply field ordering
            if (parent == null) LuaDecoder(queue, this, descriptor)
            else super.beginStructure(descriptor)

        override fun decodeNotNullMark() = queue.peek()?.value != null

        override fun toString() = "${javaClass.simpleName}(peekKey=$peekKey, queue=$queue)"

        private companion object {
            private fun toQueue(value: LuaValue): Deque<Map.Entry<*, *>> = when {
                value.isuserdata() -> (value.checkuserdata(Map::class.java) as Map<*, *>)
                value.istable() -> value.checktable().keys().associate { it.checkjstring() to value[it] }
                else -> error("Unknown $value")
            }.entries.let(::LinkedList)
        }


    }

    class LuaListDecoder(private val queue: Deque<*>, parent: AbstractLuaDecoder? = null) : AbstractLuaDecoder(parent) {
        constructor(
            luaValue: LuaValue,
            parent: AbstractLuaDecoder? = null
        ) : this(toQueue(luaValue), parent)

        private var index = 0

        override val popValue: Any? get() = queue.pop()

        override fun decodeElementIndex(descriptor: SerialDescriptor) = when {
            queue.isEmpty() -> CompositeDecoder.DECODE_DONE
            else -> index++
        }

        override fun beginStructure(descriptor: SerialDescriptor) =
            // we use a listDecoder directly when there is only 1 child, and it is a list
            if (descriptor.kind == StructureKind.LIST) this
            else super.beginStructure(descriptor)

        override fun decodeCollectionSize(descriptor: SerialDescriptor) = queue.size

        override fun decodeSequentially() = true

        private companion object {
            private fun toQueue(value: LuaValue): Deque<*> = when {
                value.isuserdata() -> (value.checkuserdata(List::class.java) as List<*>)
                value.istable() -> value.checktable().keys().map { value[it] }
                else -> error("Unknown $value")
            }.let(::LinkedList)
        }
    }

    class LuaMapDecoder(
        luaValue: LuaValue,
        parent: AbstractLuaDecoder?,
        private val targetDescriptor: SerialDescriptor
    ) : LuaDecoder(luaValue, parent) {
        private var isReadingKey = true
        private var index = 0

        init {
            // fugly workaround for deserializing straight from map
            if (parent == null) this.parent = this
        }

        override fun decodeElementIndex(descriptor: SerialDescriptor) = when {
            queue.isEmpty() -> CompositeDecoder.DECODE_DONE
            targetDescriptor != descriptor -> 0 // Nested structure with single variable
            else -> if (isReadingKey) 0 else 1
        }

        override fun beginStructure(descriptor: SerialDescriptor) =
            if (descriptor == targetDescriptor || (descriptor.elementsCount == 1 && descriptor.getElementDescriptor(0) == targetDescriptor)) this
            else super.beginStructure(descriptor)

        override val popValue: Any?
            get() = if (isReadingKey) {
                isReadingKey = false
                queue.peek()?.key
            } else {
                isReadingKey = true
                ++index
                super.popValue
            }
    }

    class LuaDirectDecoder(private val data: Any?, parent: AbstractLuaDecoder? = null) : AbstractLuaDecoder(parent) {
        private var read = false

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            if (!read) repeat(descriptor.elementsCount) {
                // Single non-optional can get serialized as direct value
                if (!descriptor.isElementOptional(it)) return it
            }
            return CompositeDecoder.DECODE_DONE
        }

        override val popValue: Any?
            get() = data.takeUnless { read }?.also { read = true }
    }
}

private val Any?.asJava: Any? get() = if (this is LuaValue) CoerceLuaToJava.coerce(this, Any::class.java) else this
