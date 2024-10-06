package be.bluexin.mcui.util

import java.util.*

class LayeredMap<K, V> : Map<K, V> {
    private val stack = LinkedList<Map<K, V>>()

    override val entries: Set<Map.Entry<K, V>>
        get() = stack.flatMap { it.entries }.toSet()
    override val keys: Set<K>
        get() = stack.flatMap { it.keys }.toSet()
    override val size: Int
        get() = keys.size
    override val values: Collection<V>
        get() = stack.flatMap { it.values }

    override fun containsKey(key: K): Boolean = stack.any { it.containsKey(key) }

    override fun containsValue(value: V): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(key: K): V? = stack.asSequence()
        .mapNotNull { it[key] }.firstOrNull()

    override fun isEmpty(): Boolean = stack.all(Map<*, *>::isEmpty)

    operator fun plusAssign(context: Map<K, V>) {
        stack.push(context)
        stack.iterator()
    }

    fun pop() {
        stack.pop()
    }

    val canPop get() = stack.isNotEmpty()
}