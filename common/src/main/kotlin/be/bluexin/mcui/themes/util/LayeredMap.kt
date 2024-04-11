package be.bluexin.mcui.themes.util

import java.util.*

class LayeredMap<K, V> : Map<K, V> {
    private val stack = LinkedList<Map<K, V>>()

    override val entries: Set<Map.Entry<K, V>>
        get() = TODO("Not yet implemented")
    override val keys: Set<K>
        get() = stack.flatMap { it.keys }.toSet()
    override val size: Int
        get() = TODO("Not yet implemented")
    override val values: Collection<V>
        get() = TODO("Not yet implemented")

    override fun containsKey(key: K): Boolean = stack.any { it.containsKey(key) }

    override fun containsValue(value: V): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(key: K): V? = stack.asSequence()
        .mapNotNull { it[key] }.firstOrNull()

    override fun isEmpty(): Boolean = stack.all(Map<*, *>::isEmpty)

    operator fun plusAssign(context: Map<K, V>) {
        stack.push(context)
    }

    fun pop() {
        stack.pop()
    }
}